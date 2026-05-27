package br.gov.sifap.pagamentos.domain;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CalculadoraDescontosTest {

    @Test @DisplayName("REQ-PAY-040: contribuição social 3% para base ≤ 500")
    void contribFaixa1() {
        var r = CalculadoraDescontos.calcular(new BigDecimal("500.00"),
                List.of(new CalculadoraDescontos.EntradaDesconto(TipoDesconto.C, null, null)));
        assertEquals(new BigDecimal("15.00"), r.total());
    }

    @Test @DisplayName("REQ-PAY-041: contribuição social 9% para base > 2000")
    void contribFaixa4() {
        var r = CalculadoraDescontos.calcular(new BigDecimal("3000.00"),
                List.of(new CalculadoraDescontos.EntradaDesconto(TipoDesconto.C, null, null)));
        assertEquals(new BigDecimal("270.00"), r.total());
    }

    @Test @DisplayName("REQ-PAY-042: teto 30% aplicado proporcionalmente aos não-judiciais")
    void teto30Pct() {
        var r = CalculadoraDescontos.calcular(new BigDecimal("1000.00"),
                List.of(
                    new CalculadoraDescontos.EntradaDesconto(TipoDesconto.I, new BigDecimal("400.00"), null),
                    new CalculadoraDescontos.EntradaDesconto(TipoDesconto.A, new BigDecimal("300.00"), null)
                ));
        // 700 solicitado, teto 300 → proporcional: I=400/700 e A=300/700 de 300
        assertEquals(new BigDecimal("300.00"), r.total());
    }

    @Test @DisplayName("REQ-PAY-043: judicial isenta do teto e somada por cima")
    void judicialIsenta() {
        var r = CalculadoraDescontos.calcular(new BigDecimal("1000.00"),
                List.of(
                    new CalculadoraDescontos.EntradaDesconto(TipoDesconto.J, new BigDecimal("500.00"), null),
                    new CalculadoraDescontos.EntradaDesconto(TipoDesconto.I, new BigDecimal("400.00"), null)
                ));
        // I respeita teto 300; J adiciona 500 sem teto → 800
        assertEquals(new BigDecimal("800.00"), r.total());
    }
}
