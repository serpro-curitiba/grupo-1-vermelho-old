package br.gov.sifap.pagamentos.domain;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Value object para competência no formato AAAAMM.
 *
 * <p>REQ-PAY-001 / REQ-PAY-003.</p>
 *
 * @param valor string AAAAMM (ex.: "202606")
 */
public record Competencia(String valor) {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMM");

    public Competencia {
        Objects.requireNonNull(valor, "competencia obrigatória");
        if (!valor.matches("^[0-9]{6}$")) {
            throw new IllegalArgumentException("competencia.formato-invalido: " + valor);
        }
        // valida que é um YearMonth real (ex.: 202613 falha)
        YearMonth.parse(valor, FMT);
    }

    public YearMonth toYearMonth() {
        return YearMonth.parse(valor, FMT);
    }

    public boolean isFuturaEm(YearMonth referencia) {
        return toYearMonth().isAfter(referencia);
    }
}
