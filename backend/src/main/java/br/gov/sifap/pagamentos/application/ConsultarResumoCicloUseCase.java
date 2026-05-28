package br.gov.sifap.pagamentos.application;

import br.gov.sifap.pagamentos.infrastructure.persistence.CicloPagamentoRepository;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsultarResumoCicloUseCase {

    private final CicloPagamentoRepository cicloRepo;

    public ConsultarResumoCicloUseCase(CicloPagamentoRepository cicloRepo) {
        this.cicloRepo = cicloRepo;
    }

    @Transactional(readOnly = true)
    public ResumoCiclo executar(UUID cicloId) {
        var resumo = cicloRepo.findResumoByCicloId(cicloId)
                .orElseThrow(() -> new CicloNaoEncontradoException(cicloId));

        return new ResumoCiclo(
                resumo.getCicloId(),
                resumo.getCompetencia(),
                resumo.getStatus(),
                resumo.getTotalCandidatos(),
                resumo.getTotalPagamentos(),
                resumo.getTotalIgnorados(),
                resumo.getTotalRejeitados(),
                resumo.getValorTotal(),
                resumo.getTotalBruto(),
                resumo.getTotalDesconto(),
                resumo.getTotalLiquido(),
                resumo.getTotalAbono(),
                resumo.getTotal13());
    }

    public record ResumoCiclo(
            UUID cicloId,
            String competencia,
            String status,
            int totalCandidatos,
            int totalPagamentos,
            int totalIgnorados,
            int totalRejeitados,
            BigDecimal valorTotal,
            BigDecimal totalBruto,
            BigDecimal totalDesconto,
            BigDecimal totalLiquido,
            BigDecimal totalAbono,
            BigDecimal total13) {}
}