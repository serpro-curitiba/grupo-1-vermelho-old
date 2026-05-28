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

    private static final BigDecimal LIMITE_DIVERGENCIA = new BigDecimal("0.01");

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
        int conciliados = 0;
        int divergentes = 0;
        int naoEncontrados = 0;

        for (RegistroCnab240 r : registros) {
            var processamento = processarRegistro(r, competencia);
            if (processamento.conciliado()) {
                conciliados++;
            }
            if (processamento.divergente()) {
                divergentes++;
            }
            if (processamento.naoEncontrado()) {
                naoEncontrados++;
            }

            registroRepo.save(new RegistroConciliacaoJpaEntity(
                    UUID.randomUUID(), arquivo.getId(), r.cpf(), r.numPagto(),
                    r.valorRetorno(), r.dtPagamento(), r.codRetorno(),
                    processamento.resultado(), processamento.diferenca()));
        }

        arquivo.setQtdLidos(registros.size());
        arquivo.setQtdConciliados(conciliados);
        arquivo.setQtdDivergentes(divergentes);
        arquivo.setQtdNaoEncontrados(naoEncontrados);

        return new ResultadoConciliacao(
                arquivo.getId(), registros.size(), conciliados, divergentes, naoEncontrados);
    }

    private ProcessamentoRegistro processarRegistro(RegistroCnab240 registro, String competencia) {
        PagamentoJpaEntity pagamento = localizarPagamento(registro, competencia);
        if (pagamento == null) {
            return new ProcessamentoRegistro("NAO_ENCONTRADO", null, false, false, true);
        }

        CodigoRetorno codigoRetorno = CodigoRetorno.doLegado(registro.codRetorno());
        BigDecimal diferenca = pagamento.getVlrLiquido().subtract(registro.valorRetorno()).abs();

        atualizarStatusPagamento(pagamento, codigoRetorno);

        if (diferenca.compareTo(LIMITE_DIVERGENCIA) > 0) {
            return new ProcessamentoRegistro("DIVERGENTE", diferenca, false, true, false);
        }

        return switch (codigoRetorno) {
            case PAGO -> new ProcessamentoRegistro("CONCILIADO", diferenca, true, false, false);
            case ESTORNADO -> new ProcessamentoRegistro("ESTORNO", diferenca, false, true, false);
            case DEVOLVIDO, REJEITADO -> new ProcessamentoRegistro("DIVERGENTE", diferenca, false, false, false);
        };
    }

    private PagamentoJpaEntity localizarPagamento(RegistroCnab240 registro, String competencia) {
        if (registro.numPagto() != null) {
            var porNumero = pagamentoRepo.findFirstByNumPagto(registro.numPagto());
            if (porNumero.isPresent()) {
                return porNumero.get();
            }
        }
        if (registro.cpf() != null) {
            return pagamentoRepo.findFirstByCpfAndCompetencia(registro.cpf(), competencia).orElse(null);
        }
        return null;
    }

    private static void atualizarStatusPagamento(PagamentoJpaEntity pagamento, CodigoRetorno codigoRetorno) {
        pagamento.setStatus(switch (codigoRetorno) {
            case PAGO -> StatusPagamento.PAGO.name();
            case DEVOLVIDO, REJEITADO -> StatusPagamento.REJEITADO.name();
            case ESTORNADO -> StatusPagamento.CANCELADO.name();
        });
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

        private record ProcessamentoRegistro(
            String resultado,
            BigDecimal diferenca,
            boolean conciliado,
            boolean divergente,
            boolean naoEncontrado) {}
}
