package br.gov.sifap.conciliacao.application;

import br.gov.sifap.conciliacao.domain.*;
import br.gov.sifap.conciliacao.infrastructure.persistence.*;
import br.gov.sifap.pagamentos.domain.StatusPagamento;
import br.gov.sifap.pagamentos.infrastructure.persistence.PagamentoJpaEntity;
import br.gov.sifap.pagamentos.infrastructure.persistence.PagamentoRepository;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Substitui {@code BATCHCON.NSN} (linhas 60-410).
 * Lê arquivo CNAB 240 do BB, casa com a tabela {@code pagamentos.pagamento}
 * pelo {@code num_pagto} (ou CPF + competência) e marca cada pagamento como
 * PAGO / DEVOLVIDO / ESTORNADO. Diferença &gt; 0.01 = DIVERGENTE.
 */
@Service
public class ConciliarRemessaUseCase {

    private final ArquivoConciliacaoRepository arquivoRepo;
    private final RegistroConciliacaoRepository registroRepo;
    private final PagamentoRepository pagamentoRepo;
    private final Clock clock;

    public ConciliarRemessaUseCase(ArquivoConciliacaoRepository a,
                                   RegistroConciliacaoRepository r,
                                   PagamentoRepository p, Clock clock) {
        this.arquivoRepo = a; this.registroRepo = r;
        this.pagamentoRepo = p; this.clock = clock;
    }

    @Transactional
    public ResultadoConciliacao executar(String nomeArquivo, String competencia, InputStream in)
            throws IOException {
        byte[] bytes = in.readAllBytes();
        String sha = sha256(bytes);

        var arquivo = new ArquivoConciliacaoJpaEntity(
                UUID.randomUUID(), nomeArquivo, competencia,
                OffsetDateTime.now(clock), sha);
        arquivoRepo.save(arquivo);

        List<RegistroCnab240> registros = ParserCnab240.ler(new java.io.ByteArrayInputStream(bytes));
        int conciliados = 0, divergentes = 0, naoEncontrados = 0;

        for (RegistroCnab240 r : registros) {
            PagamentoJpaEntity pgto = null;
            if (r.numPagto() != null) {
                pgto = pagamentoRepo.findFirstByNumPagto(r.numPagto()).orElse(null);
            }
            if (pgto == null && r.cpf() != null) {
                pgto = pagamentoRepo.findFirstByCpfAndCompetencia(r.cpf(), competencia).orElse(null);
            }

            String resultado;
            BigDecimal dif = null;
            CodigoRetorno cr = CodigoRetorno.doLegado(r.codRetorno());

            if (pgto == null) {
                resultado = "NAO_ENCONTRADO";
                naoEncontrados++;
            } else {
                dif = pgto.getVlrLiquido().subtract(r.valorRetorno()).abs();
                if (dif.compareTo(new BigDecimal("0.01")) > 0) {
                    resultado = "DIVERGENTE";
                    divergentes++;
                } else {
                    resultado = switch (cr) {
                        case PAGO       -> "CONCILIADO";
                        case ESTORNADO  -> "ESTORNO";
                        case DEVOLVIDO, REJEITADO -> "DIVERGENTE";
                    };
                    if ("CONCILIADO".equals(resultado)) conciliados++;
                    else if ("ESTORNO".equals(resultado)) divergentes++;
                }
                // atualiza status do pagamento
                pgto.setStatus(switch (cr) {
                    case PAGO       -> StatusPagamento.PAGO.name();
                    case DEVOLVIDO  -> StatusPagamento.REJEITADO.name();
                    case ESTORNADO  -> StatusPagamento.CANCELADO.name();
                    case REJEITADO  -> StatusPagamento.REJEITADO.name();
                });
            }

            registroRepo.save(new RegistroConciliacaoJpaEntity(
                    UUID.randomUUID(), arquivo.getId(), r.cpf(), r.numPagto(),
                    r.valorRetorno(), r.dtPagamento(), r.codRetorno(),
                    resultado, dif));
        }

        arquivo.setQtdLidos(registros.size());
        arquivo.setQtdConciliados(conciliados);
        arquivo.setQtdDivergentes(divergentes);
        arquivo.setQtdNaoEncontrados(naoEncontrados);

        return new ResultadoConciliacao(
                arquivo.getId(), registros.size(), conciliados, divergentes, naoEncontrados);
    }

    private static String sha256(byte[] b) {
        try {
            var md = MessageDigest.getInstance("SHA-256");
            byte[] h = md.digest(b);
            var sb = new StringBuilder();
            for (byte x : h) sb.append(String.format("%02x", x));
            return sb.toString();
        } catch (Exception e) { return null; }
    }

    public record ResultadoConciliacao(UUID arquivoId, int qtdLidos, int qtdConciliados,
                                       int qtdDivergentes, int qtdNaoEncontrados) {}
}
