package br.gov.sifap.shared;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Replica o padrão de truncamento monetário do legado Natural
 * (CALCBENF: {@code compute temp = val * 100;  val = temp / 100}).
 *
 * <p><b>Importante:</b> NÃO arredonda — o legado descarta as casas extras.
 * BATCHREL.NSN, em contraste, arredonda com {@code +0.005}.</p>
 */
public final class TruncamentoMonetario {

    private TruncamentoMonetario() {}

    /** Trunca em 2 casas (paridade CALCBENF). */
    public static BigDecimal truncar(BigDecimal valor) {
        if (valor == null) return BigDecimal.ZERO;
        return valor.setScale(2, RoundingMode.DOWN);
    }

    /** Arredonda HALF_UP em 2 casas (paridade BATCHREL). */
    public static BigDecimal arredondar(BigDecimal valor) {
        if (valor == null) return BigDecimal.ZERO;
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}
