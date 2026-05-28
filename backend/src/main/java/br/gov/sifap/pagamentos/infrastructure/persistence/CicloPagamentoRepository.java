package br.gov.sifap.pagamentos.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CicloPagamentoRepository extends JpaRepository<CicloPagamentoJpaEntity, UUID> {

    /**
     * Verifica a existência de um ciclo NÃO cancelado para uma competência.
     * Suporta REQ-PAY-002 (bloqueio de duplicidade).
     */
    Optional<CicloPagamentoJpaEntity> findFirstByCompetenciaAndStatusNot(String competencia, String status);

    @Query(value = """
            SELECT
                c.id AS cicloId,
                c.competencia AS competencia,
                CASE
                    WHEN c.status = 'CALCULANDO' AND COUNT(p.id) > 0 THEN 'CALCULADO'
                    ELSE c.status
                END AS status,
                CASE
                    WHEN c.total_candidatos > 0 THEN c.total_candidatos
                    ELSE (COUNT(p.id) + c.total_ignorados)
                END AS totalCandidatos,
                GREATEST(c.total_pagamentos, COUNT(p.id)) AS totalPagamentos,
                c.total_ignorados AS totalIgnorados,
                COALESCE(SUM(CASE WHEN p.status = 'REJEITADO' THEN 1 ELSE 0 END), 0)::INT AS totalRejeitados,
                CASE
                    WHEN c.valor_total > 0 THEN c.valor_total
                    ELSE COALESCE(SUM(p.vlr_liquido), 0)
                END AS valorTotal,
                CASE
                    WHEN c.total_bruto > 0 THEN c.total_bruto
                    ELSE COALESCE(SUM(p.valor_final), 0)
                END AS totalBruto,
                CASE
                    WHEN c.total_desconto > 0 THEN c.total_desconto
                    ELSE COALESCE(SUM(p.vlr_desconto), 0)
                END AS totalDesconto,
                CASE
                    WHEN c.total_liquido > 0 THEN c.total_liquido
                    ELSE COALESCE(SUM(p.vlr_liquido), 0)
                END AS totalLiquido,
                CASE
                    WHEN c.total_abono > 0 THEN c.total_abono
                    ELSE COALESCE(SUM(p.vlr_abono), 0)
                END AS totalAbono,
                CASE
                    WHEN c.total_13 > 0 THEN c.total_13
                    ELSE COALESCE(SUM(p.vlr_13), 0)
                END AS total13
            FROM pagamentos.ciclo_pagamento c
            LEFT JOIN pagamentos.pagamento p ON p.ciclo_id = c.id
            WHERE c.id = :cicloId
            GROUP BY c.id, c.competencia, c.status, c.total_candidatos, c.total_pagamentos,
                     c.total_ignorados, c.valor_total, c.total_bruto, c.total_desconto,
                     c.total_liquido, c.total_abono, c.total_13
            """, nativeQuery = true)
    Optional<CicloResumoProjection> findResumoByCicloId(@Param("cicloId") UUID cicloId);

    interface CicloResumoProjection {
        UUID getCicloId();
        String getCompetencia();
        String getStatus();
        int getTotalCandidatos();
        int getTotalPagamentos();
        int getTotalIgnorados();
        int getTotalRejeitados();
        java.math.BigDecimal getValorTotal();
        java.math.BigDecimal getTotalBruto();
        java.math.BigDecimal getTotalDesconto();
        java.math.BigDecimal getTotalLiquido();
        java.math.BigDecimal getTotalAbono();
        java.math.BigDecimal getTotal13();
    }
}
