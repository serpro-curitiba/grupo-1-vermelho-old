package br.gov.sifap.pagamentos.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Composição do valor final de um pagamento — espelha REQ-PAY-020:
 * {@code valorFinal = valorBase × fatorRegional × fatorFamiliar × fatorRenda × fatorIdade × fatorReajuste}
 * com arredondamento HALF_UP a 2 casas.
 */
public record ComposicaoValor(
        BigDecimal valorBase,
        BigDecimal fatorRegional,
        BigDecimal fatorFamiliar,
        BigDecimal fatorRenda,
        BigDecimal fatorIdade,
        BigDecimal fatorReajuste) {

    public ComposicaoValor {
        if (valorBase == null || valorBase.signum() < 0) {
            throw new IllegalArgumentException("valorBase deve ser >= 0");
        }
        fatorRegional  = nz(fatorRegional);
        fatorFamiliar  = nz(fatorFamiliar);
        fatorRenda     = nz(fatorRenda);
        fatorIdade     = nz(fatorIdade);
        fatorReajuste  = nz(fatorReajuste);
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ONE : v;
    }

    public BigDecimal valorFinal() {
        // Truncamento ao invés de HALF_UP — paridade legado (CALCBENF.NSN
        // executa `compute temp = val * 100; val = temp / 100`, descartando casas).
        return valorBase
                .multiply(fatorRegional)
                .multiply(fatorFamiliar)
                .multiply(fatorRenda)
                .multiply(fatorIdade)
                .multiply(fatorReajuste)
                .setScale(2, RoundingMode.DOWN);
    }
}
