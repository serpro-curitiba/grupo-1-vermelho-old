package br.gov.sifap.pagamentos.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CalculadoraBeneficioTest {

    private final CalculadoraBeneficio calc = new CalculadoraBeneficio(
            Map.of(1, new BigDecimal("1.2000"), 25, new BigDecimal("0.8000")),
            new BigDecimal("1.0000"));

    private BeneficiarioSnapshot snap(int regiao, int deps, String renda, int idade) {
        return new BeneficiarioSnapshot(UUID.randomUUID(), "11122233344", "X",
                regiao, deps, new BigDecimal(renda), idade, "ATIVO", "PROG",
                OffsetDateTime.now());
    }

    @Test
    @DisplayName("REQ-PAY-021: região 1 aplica fator 1.20")
    void fatorRegional_regiaoConhecida() {
        assertThat(calc.fatorRegional(1)).isEqualByComparingTo("1.2000");
    }

    @Test
    @DisplayName("REQ-PAY-021: região fora de 1..25 recai para 1.0 (descarte MYS-002)")
    void fatorRegional_foraDoIntervalo_caiParaUm() {
        assertThat(calc.fatorRegional(26)).isEqualByComparingTo("1.0000");
        assertThat(calc.fatorRegional(0)).isEqualByComparingTo("1.0000");
    }

    @Test
    @DisplayName("REQ-PAY-022: +5% por dependente até teto de 4")
    void fatorFamiliar_temTeto() {
        assertThat(calc.fatorFamiliar(0)).isEqualByComparingTo("1.0000");
        assertThat(calc.fatorFamiliar(2)).isEqualByComparingTo("1.1000");
        assertThat(calc.fatorFamiliar(4)).isEqualByComparingTo("1.2000");
        assertThat(calc.fatorFamiliar(10)).isEqualByComparingTo("1.2000");
    }

    @Test
    @DisplayName("REQ-PAY-023: renda < 3000 ganha +10%; >= 3000 perde 10%")
    void fatorRenda() {
        assertThat(calc.fatorRenda(new BigDecimal("1500"))).isEqualByComparingTo("1.1000");
        assertThat(calc.fatorRenda(new BigDecimal("3000"))).isEqualByComparingTo("0.9000");
    }

    @Test
    @DisplayName("REQ-PAY-024: idoso >=65 ganha +5%")
    void fatorIdade() {
        assertThat(calc.fatorIdade(64)).isEqualByComparingTo("1.0000");
        assertThat(calc.fatorIdade(65)).isEqualByComparingTo("1.0500");
    }

    @Test
    @DisplayName("REQ-PAY-020: fórmula combinada com HALF_UP a 2 casas")
    void formulaCompleta_arredonda() {
        ComposicaoValor cv = calc.calcular(new BigDecimal("600.00"), snap(1, 2, "1500", 70));
        // 600 * 1.20 * 1.10 * 1.10 * 1.05 * 1.00 = 914.76
        assertThat(cv.valorFinal()).isEqualByComparingTo("914.76");
    }

    @Test
    @DisplayName("ComposicaoValor: valorBase negativo é rejeitado")
    void composicao_rejeitaValorBaseNegativo() {
        assertThatThrownBy(() -> new ComposicaoValor(new BigDecimal("-1"),
                null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
