package br.gov.sifap.pagamentos.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.YearMonth;
import org.junit.jupiter.api.Test;

class CompetenciaTest {

    @Test
    void aceitaFormatoValido() {
        assertThat(new Competencia("202606").toYearMonth()).isEqualTo(YearMonth.of(2026, 6));
    }

    @Test
    void rejeitaFormatoInvalido() {
        assertThatThrownBy(() -> new Competencia("2026/06"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("competencia.formato-invalido");
    }

    @Test
    void rejeitaMesInexistente() {
        assertThatThrownBy(() -> new Competencia("202613"))
                .isInstanceOf(java.time.format.DateTimeParseException.class);
    }

    @Test
    void detectaCompetenciaFutura() {
        Competencia futura = new Competencia("203012");
        assertThat(futura.isFuturaEm(YearMonth.of(2026, 6))).isTrue();
    }
}
