package br.gov.sifap.pagamentos.domain;

import br.gov.sifap.shared.TruncamentoMonetario;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Domain service que aplica os descontos conforme {@code CALCDSCT.NSN}
 * (linhas 60-280).
 *
 * <p>Regras:</p>
 * <ul>
 *   <li>Contribuição social (C): tabela progressiva
 *       ≤500=3%, ≤1000=5%, ≤2000=7%, &gt;2000=9% sobre o valor bruto;</li>
 *   <li>Sindical (S): 1% fixo sobre o valor bruto;</li>
 *   <li>I/P/A: percentual ou valor fixo cadastrado;</li>
 *   <li>Judicial (J): percentual ou valor fixo — <b>não</b> sofre teto;</li>
 *   <li><b>Teto de 30%</b> sobre o bruto para a soma de TODOS os descontos
 *       EXCETO judicial (linhas 250-275). Se excedido, descontos não-judiciais
 *       são proporcionalmente reduzidos.</li>
 * </ul>
 */
public final class CalculadoraDescontos {

    private static final BigDecimal TETO_PCT = new BigDecimal("0.30");
    private static final BigDecimal SINDICAL_PCT = new BigDecimal("0.01");

    private CalculadoraDescontos() {}

    public static Resultado calcular(BigDecimal valorBruto, List<EntradaDesconto> entradas) {
        if (entradas == null || entradas.isEmpty()) {
            return new Resultado(BigDecimal.ZERO, List.of());
        }
        var itens = new java.util.ArrayList<ItemAplicado>();
        BigDecimal totalNaoJud = BigDecimal.ZERO;
        BigDecimal totalJud = BigDecimal.ZERO;

        for (var e : entradas) {
            BigDecimal v = valorDesconto(valorBruto, e);
            v = TruncamentoMonetario.truncar(v);
            itens.add(new ItemAplicado(e.tipo(), v));
            if (e.tipo() == TipoDesconto.JUDICIAL) totalJud = totalJud.add(v);
            else                                    totalNaoJud = totalNaoJud.add(v);
        }

        BigDecimal teto = valorBruto.multiply(TETO_PCT);
        if (totalNaoJud.compareTo(teto) > 0 && totalNaoJud.signum() > 0) {
            BigDecimal proporcao = teto.divide(totalNaoJud, 6, RoundingMode.DOWN);
            var ajustado = new java.util.ArrayList<ItemAplicado>();
            BigDecimal somaNova = BigDecimal.ZERO;
            for (var it : itens) {
                if (it.tipo() == TipoDesconto.JUDICIAL) { ajustado.add(it); continue; }
                BigDecimal novo = TruncamentoMonetario.truncar(it.valor().multiply(proporcao));
                ajustado.add(new ItemAplicado(it.tipo(), novo));
                somaNova = somaNova.add(novo);
            }
            return new Resultado(somaNova.add(totalJud), ajustado);
        }
        return new Resultado(totalNaoJud.add(totalJud), itens);
    }

    private static BigDecimal valorDesconto(BigDecimal bruto, EntradaDesconto e) {
        return switch (e.tipo()) {
            case CONTRIBUICAO -> contribuicaoSocial(bruto);
            case SINDICAL     -> bruto.multiply(SINDICAL_PCT);
            default -> {
                if (e.valorFixo() != null) yield e.valorFixo();
                if (e.percentual() != null)
                    yield bruto.multiply(e.percentual().divide(new BigDecimal("100"), 6, RoundingMode.DOWN));
                yield BigDecimal.ZERO;
            }
        };
    }

    static BigDecimal contribuicaoSocial(BigDecimal bruto) {
        BigDecimal pct;
        if (bruto.compareTo(new BigDecimal("500"))  <= 0) pct = new BigDecimal("0.03");
        else if (bruto.compareTo(new BigDecimal("1000")) <= 0) pct = new BigDecimal("0.05");
        else if (bruto.compareTo(new BigDecimal("2000")) <= 0) pct = new BigDecimal("0.07");
        else pct = new BigDecimal("0.09");
        return bruto.multiply(pct);
    }

    public record EntradaDesconto(TipoDesconto tipo, BigDecimal valorFixo, BigDecimal percentual) {}
    public record ItemAplicado(TipoDesconto tipo, BigDecimal valor) {}
    public record Resultado(BigDecimal total, List<ItemAplicado> itens) {}
}
