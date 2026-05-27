package br.gov.sifap.programas.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calcula o FATOR-K do {@code CADPROG.NSN} (subroutine CALC-FATOR-K, linhas 152-176).
 *
 * <p>Fórmula histórica (não documentada no DDM, descoberta no programa Natural):
 * <pre>
 *     fator-k = 1.00 + (pct-reajuste-anual * 0.347215)
 *     vlr-calc-ajustado = vlr-base * fator-k
 * </pre>
 * A constante {@code 0.347215} permanece um <b>mistério arqueológico</b>
 * (provável conversão URV/Real). Mantida para paridade de cálculo.</p>
 */
public final class FatorK {

    private static final BigDecimal CONSTANTE_HISTORICA = new BigDecimal("0.347215");

    private FatorK() {}

    public static BigDecimal calcular(BigDecimal pctReajusteAnual) {
        BigDecimal r = pctReajusteAnual == null ? BigDecimal.ZERO : pctReajusteAnual;
        return BigDecimal.ONE.add(r.multiply(CONSTANTE_HISTORICA))
                .setScale(4, RoundingMode.HALF_UP);
    }

    public static BigDecimal aplicar(BigDecimal vlrBase, BigDecimal pctReajusteAnual) {
        BigDecimal base = vlrBase == null ? BigDecimal.ZERO : vlrBase;
        return base.multiply(calcular(pctReajusteAnual)).setScale(2, RoundingMode.DOWN);
    }
}
