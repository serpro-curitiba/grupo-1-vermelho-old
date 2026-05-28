package br.gov.sifap.relatorios.application;

import br.gov.sifap.shared.TruncamentoMonetario;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Substitui BATCHREL.NSN (relatório consolidado por região × status).
 *
 * <p><b>Particularidade legada:</b> diferentemente de CALCBENF (truncamento),
 * BATCHREL utiliza <em>arredondamento</em> bancário HALF_UP. Mantido por
 * paridade — qualquer correção exige aprovação do PO.</p>
 *
 * <p>Mapeamento de regiões (igual ao Natural):</p>
 * <pre>
 *   1-5   → N   (Norte)
 *   6-10  → NE  (Nordeste)
 *   11-15 → SE  (Sudeste)
 *   16-20 → S   (Sul)
 *   21+   → CO  (Centro-Oeste)
 * </pre>
 */
@Service
public class RelatorioConsolidadoUseCase {

    private final JdbcTemplate jdbc;

    public RelatorioConsolidadoUseCase(DataSource ds) {
        this.jdbc = new JdbcTemplate(ds);
    }

    public List<LinhaConsolidada> executar(String competencia) {
        // Carrega regiao do beneficiário para classificar
        String sql = """
                SELECT
              b.regiao            AS regiao,
                  p.status            AS status,
                  COUNT(*)            AS qtd,
                  COALESCE(SUM(p.vlr_liquido), 0) AS total
                FROM pagamentos.pagamento p
                JOIN beneficiarios.beneficiario b ON b.id = p.beneficiario_id
                WHERE p.competencia = ?
            GROUP BY b.regiao, p.status
            ORDER BY b.regiao, p.status
                """;
        var bruto = jdbc.queryForList(sql, competencia);
        var agrup = new java.util.LinkedHashMap<String, java.util.Map<String, Acumulador>>();
        for (var row : bruto) {
            Integer regiao = (Integer) row.get("regiao");
            String  status = (String)  row.get("status");
            Number  qtd    = (Number)  row.get("qtd");
            BigDecimal tot = row.get("total") instanceof BigDecimal b ? b
                          : new BigDecimal(String.valueOf(row.get("total")));
            String macroRegiao = macro(regiao);
            agrup
                .computeIfAbsent(macroRegiao, k -> new java.util.LinkedHashMap<>())
                .computeIfAbsent(status, k -> new Acumulador())
                .acumular(qtd.intValue(), tot);
        }
        var out = new ArrayList<LinhaConsolidada>();
        for (var e1 : agrup.entrySet()) {
            for (var e2 : e1.getValue().entrySet()) {
                out.add(new LinhaConsolidada(
                        competencia, e1.getKey(), e2.getKey(),
                        e2.getValue().qtd,
                        TruncamentoMonetario.arredondar(e2.getValue().total)));
            }
        }
        return out;
    }

    private static String macro(Integer numRegiao) {
        if (numRegiao == null) return "ZZ";
        int r = numRegiao;
        if (r <= 5)  return "N";
        if (r <= 10) return "NE";
        if (r <= 15) return "SE";
        if (r <= 20) return "S";
        return "CO";
    }

    private static class Acumulador {
        int qtd;
        BigDecimal total = BigDecimal.ZERO;
        void acumular(int q, BigDecimal t) { qtd += q; total = total.add(t); }
    }

    public record LinhaConsolidada(String competencia, String macroRegiao, String status,
                                   int quantidade, BigDecimal totalArredondado) {}
}
