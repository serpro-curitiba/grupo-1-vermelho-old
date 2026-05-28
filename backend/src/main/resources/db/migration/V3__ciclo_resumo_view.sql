-- ============================================================================
-- V3 — View de resumo operacional por ciclo (escopo ampliado front/back/banco)
-- Suporta leitura otimizada para dashboard de ciclo no frontend.
-- ============================================================================

CREATE OR REPLACE VIEW pagamentos.vw_ciclo_resumo AS
SELECT
    c.id AS ciclo_id,
    c.competencia,
    c.status,
    c.total_candidatos,
    c.total_pagamentos,
    c.total_ignorados,
    COALESCE(SUM(CASE WHEN p.status = 'REJEITADO' THEN 1 ELSE 0 END), 0)::INT AS total_rejeitados,
    c.valor_total,
    c.total_bruto,
    c.total_desconto,
    c.total_liquido,
    c.total_abono,
    c.total_13,
    c.iniciado_em,
    c.calculado_em
FROM pagamentos.ciclo_pagamento c
LEFT JOIN pagamentos.pagamento p ON p.ciclo_id = c.id
GROUP BY
    c.id,
    c.competencia,
    c.status,
    c.total_candidatos,
    c.total_pagamentos,
    c.total_ignorados,
    c.valor_total,
    c.total_bruto,
    c.total_desconto,
    c.total_liquido,
    c.total_abono,
    c.total_13,
    c.iniciado_em,
    c.calculado_em;