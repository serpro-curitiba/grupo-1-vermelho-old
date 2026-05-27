package br.gov.sifap.pagamentos.application;

import br.gov.sifap.pagamentos.domain.*;
import br.gov.sifap.pagamentos.infrastructure.persistence.*;
import br.gov.sifap.shared.TruncamentoMonetario;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso que substitui {@code BATCHPGT.NSN} (linhas 50-450).
 *
 * <p>Fluxo paritário ao legado:</p>
 * <ol>
 *   <li>valida competência (não futura, não cancelada);</li>
 *   <li>cria ciclo em CALCULANDO;</li>
 *   <li>lê beneficiários ATIVOS ordenados por CPF (downstream depende);</li>
 *   <li>para cada beneficiário: calcula bruto (CALCBENF), 13º em dezembro,
 *       abono 15% em dezembro p/ programas tipo A, descontos (CALCDSCT),
 *       respeita teto 30% exceto judicial e detecta duplicidade por
 *       (CPF, competência);</li>
 *   <li>persiste totalizadores e fecha o ciclo;</li>
 *   <li>publica evento via outbox.</li>
 * </ol>
 */
@Service
public class GerarCicloPagamentoUseCase {

    private static final Logger log = LoggerFactory.getLogger(GerarCicloPagamentoUseCase.class);
    private static final int LOG_CADA = 1000;

    private final CicloPagamentoRepository cicloRepo;
    private final PagamentoRepository pagamentoRepo;
    private final OutboxEventRepository outboxRepo;
    private final BeneficiariosPort beneficiariosPort;
    private final DescontosCadastraisJdbcLookup descontosLookup;
    private final ProgramaTipoJdbcLookup programaTipoLookup;
    private final Clock clock;

    public GerarCicloPagamentoUseCase(CicloPagamentoRepository cicloRepo,
                                      PagamentoRepository pagamentoRepo,
                                      OutboxEventRepository outboxRepo,
                                      BeneficiariosPort beneficiariosPort,
                                      DescontosCadastraisJdbcLookup descontosLookup,
                                      ProgramaTipoJdbcLookup programaTipoLookup,
                                      Clock clock) {
        this.cicloRepo = cicloRepo;
        this.pagamentoRepo = pagamentoRepo;
        this.outboxRepo = outboxRepo;
        this.beneficiariosPort = beneficiariosPort;
        this.descontosLookup = descontosLookup;
        this.programaTipoLookup = programaTipoLookup;
        this.clock = clock;
    }

    @Transactional
    public ResultadoGeracao executar(Competencia competencia, String requisitanteId) {
        YearMonth atual = YearMonth.now(clock);
        if (competencia.isFuturaEm(atual)) {
            throw new CompetenciaFuturaException(competencia.valor());
        }
        cicloRepo.findFirstByCompetenciaAndStatusNot(competencia.valor(), StatusCiclo.CANCELADO.name())
                .ifPresent(c -> { throw new CicloDuplicadoException(competencia.valor()); });

        OffsetDateTime agora = OffsetDateTime.now(clock);
        UUID cicloId = UUID.randomUUID();
        var ciclo = new CicloPagamentoJpaEntity(
                cicloId, competencia.valor(), StatusCiclo.CALCULANDO.name(), agora, requisitanteId);
        try {
            cicloRepo.saveAndFlush(ciclo);
        } catch (DataIntegrityViolationException e) {
            throw new CicloDuplicadoException(competencia.valor());
        }

        List<BeneficiarioSnapshot> ativos = beneficiariosPort.listarAtivosOrdenadosPorCpf();
        var calc = novaCalculadora();

        int mes = Integer.parseInt(competencia.valor().substring(4));
        LocalDate dtGeracao = LocalDate.now(clock);

        BigDecimal totalBruto = BigDecimal.ZERO, totalDesc = BigDecimal.ZERO;
        BigDecimal totalLiq = BigDecimal.ZERO, totalAbono = BigDecimal.ZERO;
        BigDecimal total13 = BigDecimal.ZERO;
        int totalPagtos = 0, totalIgnorados = 0;

        Set<String> cpfsVistos = new HashSet<>();
        String cpfAnterior = null;
        Map<String, BigDecimal> cacheVlrBase = new HashMap<>();

        int idx = 0;
        for (var snap : ativos) {
            idx++;
            // duplicidade por CPF anterior (BATCHPGT linhas 280-295)
            if (snap.cpf().equals(cpfAnterior) || !cpfsVistos.add(snap.cpf())) {
                totalIgnorados++; continue;
            }
            cpfAnterior = snap.cpf();
            // duplicidade contra histórico (ciclos anteriores na mesma competência)
            if (pagamentoRepo.findFirstByCpfAndCompetencia(snap.cpf(), competencia.valor()).isPresent()) {
                totalIgnorados++; continue;
            }

            BigDecimal valorBase = cacheVlrBase.computeIfAbsent(snap.programaCodigo(),
                    beneficiariosPort::valorBaseDoProgramaAtivo);

            ComposicaoValor cv = calc.calcular(valorBase, snap);
            BigDecimal bruto = cv.valorFinal();

            BigDecimal vlr13 = mes == 12 ? calc.decimoTerceiro(valorBase, snap) : BigDecimal.ZERO;
            String tipoPrograma = programaTipoLookup.tipo(snap.programaCodigo());
            BigDecimal abono = calc.abono(valorBase, tipoPrograma, mes);

            // CALCDSCT
            var entradasDesc = descontosLookup.doBeneficiario(snap.beneficiarioId()).stream()
                    .map(c -> new CalculadoraDescontos.EntradaDesconto(
                            TipoDesconto.doLegado(c.tipo()), c.valorFixo(), c.percentual()))
                    .toList();
            BigDecimal brutoCompleto = bruto.add(vlr13).add(abono);
            var dsc = CalculadoraDescontos.calcular(brutoCompleto, entradasDesc);
            BigDecimal liquido = TruncamentoMonetario.truncar(brutoCompleto.subtract(dsc.total()));

            var pgto = new PagamentoJpaEntity(
                    UUID.randomUUID(), cicloId, snap.beneficiarioId(), snap.cpf(),
                    StatusPagamento.CALCULADO.name(), cv.valorBase(),
                    cv.fatorRegional(), cv.fatorFamiliar(), cv.fatorRenda(),
                    cv.fatorIdade(), cv.fatorReajuste(), bruto, agora);
            pgto.setCodPrograma(snap.programaCodigo());
            pgto.setCompetencia(competencia.valor());
            pgto.setVlr13(vlr13);
            pgto.setVlrAbono(abono);
            pgto.setVlrDesconto(dsc.total());
            pgto.setVlrLiquido(liquido);
            pgto.setTipoPgto(mes == 12 ? "T" : "N");
            pgto.setDtGeracao(dtGeracao);
            pagamentoRepo.save(pgto);

            totalBruto = totalBruto.add(bruto);
            total13    = total13.add(vlr13);
            totalAbono = totalAbono.add(abono);
            totalDesc  = totalDesc.add(dsc.total());
            totalLiq   = totalLiq.add(liquido);
            totalPagtos++;

            if (idx % LOG_CADA == 0) {
                log.info("BATCHPGT: {} beneficiários processados (ciclo {})", idx, cicloId);
            }
        }

        ciclo.setTotalCandidatos(ativos.size());
        ciclo.setTotalPagamentos(totalPagtos);
        ciclo.setTotalIgnorados(totalIgnorados);
        ciclo.setValorTotal(totalBruto.add(total13).add(totalAbono));
        ciclo.setTotalBruto(totalBruto);
        ciclo.setTotalDesconto(totalDesc);
        ciclo.setTotalLiquido(totalLiq);
        ciclo.setTotalAbono(totalAbono);
        ciclo.setTotal13(total13);
        ciclo.setCalculadoEm(agora);
        ciclo.setStatus(StatusCiclo.CALCULADO.name());

        outboxRepo.save(new OutboxEventJpaEntity(
                UUID.randomUUID(),
                "sifap.pagamentos.ciclo.v1",
                "CicloCalculado",
                """
                {"cicloId":"%s","competencia":"%s","totalPagamentos":%d,\
                "totalBruto":%s,"totalLiquido":%s,"calculadoEm":"%s"}
                """.formatted(cicloId, competencia.valor(), totalPagtos,
                              totalBruto.toPlainString(), totalLiq.toPlainString(), agora),
                agora));

        return new ResultadoGeracao(cicloId, StatusCiclo.CALCULADO, totalPagtos, ciclo.getValorTotal());
    }

    private CalculadoraBeneficio novaCalculadora() {
        Map<Integer, BigDecimal> tabela = new HashMap<>();
        // Valores reais do CALCBENF.NSN — também carregados na tabela programas.fator_regional V2.
        BigDecimal[] vals = {
            new BigDecimal("1.3500"), new BigDecimal("1.3200"), new BigDecimal("1.3000"),
            new BigDecimal("1.2800"), new BigDecimal("1.3100"), new BigDecimal("1.4000"),
            new BigDecimal("1.3800"), new BigDecimal("1.3500"), new BigDecimal("1.3200"),
            new BigDecimal("1.3600"), new BigDecimal("1.1000"), new BigDecimal("1.1200"),
            new BigDecimal("1.0800"), new BigDecimal("1.0500"), new BigDecimal("1.0000"),
            new BigDecimal("1.0500"), new BigDecimal("1.0700"), new BigDecimal("1.0300"),
            new BigDecimal("1.1500"), new BigDecimal("1.2000"), new BigDecimal("1.1800"),
            new BigDecimal("1.2500"), new BigDecimal("1.1000"), new BigDecimal("1.2200"),
            new BigDecimal("1.3300"), new BigDecimal("1.0000"), new BigDecimal("1.0000")
        };
        for (int i = 0; i < vals.length; i++) tabela.put(i + 1, vals[i]);
        return new CalculadoraBeneficio(tabela, new BigDecimal("1.0000"));
    }

    public record ResultadoGeracao(UUID cicloId, StatusCiclo status,
                                   int totalPagamentos, BigDecimal valorTotal) {}
}
