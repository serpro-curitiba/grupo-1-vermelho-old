package br.gov.sifap.relatorios.application;

import br.gov.sifap.shared.CpfMask;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Substitui RELPGT.NSN — relatório analítico de pagamentos por período,
 * com filtro opcional de programa, mascarando CPF (LGPD).
 */
@Service
public class RelatorioAnaliticoUseCase {

    private final JdbcTemplate jdbc;

    public RelatorioAnaliticoUseCase(DataSource ds) {
        this.jdbc = new JdbcTemplate(ds);
    }

    public List<LinhaAnalitica> executar(LocalDate de, LocalDate ate, String codPrograma) {
        StringBuilder sql = new StringBuilder("""
                SELECT p.dt_geracao, p.competencia, p.cod_programa, p.cpf, p.status,
                       p.vlr_base, p.vlr_desconto, p.vlr_liquido, p.vlr_abono, p.vlr_13
                FROM pagamentos.pagamento p
                WHERE p.dt_geracao BETWEEN ? AND ?
                """);
        Object[] params;
        if (codPrograma != null && !codPrograma.isBlank()) {
            sql.append(" AND p.cod_programa = ? ");
            params = new Object[]{de, ate, codPrograma};
        } else {
            params = new Object[]{de, ate};
        }
        sql.append(" ORDER BY p.cod_programa, p.dt_geracao, p.cpf ");
        var rows = jdbc.queryForList(sql.toString(), params);
        var out = new ArrayList<LinhaAnalitica>(rows.size());
        for (var r : rows) {
            out.add(new LinhaAnalitica(
                    r.get("dt_geracao") == null ? null : r.get("dt_geracao").toString(),
                    (String) r.get("competencia"),
                    (String) r.get("cod_programa"),
                    CpfMask.mask((String) r.get("cpf")),
                    (String) r.get("status"),
                    big(r.get("vlr_base")),
                    big(r.get("vlr_desconto")),
                    big(r.get("vlr_liquido")),
                    big(r.get("vlr_abono")),
                    big(r.get("vlr_13"))));
        }
        return out;
    }

    private static BigDecimal big(Object v) {
        if (v == null) return BigDecimal.ZERO;
        return v instanceof BigDecimal b ? b : new BigDecimal(v.toString());
    }

    public record LinhaAnalitica(String dtGeracao, String competencia, String codPrograma,
                                 String cpfMascarado, String status, BigDecimal vlrBase,
                                 BigDecimal vlrDesconto, BigDecimal vlrLiquido,
                                 BigDecimal vlrAbono, BigDecimal vlr13) {}
}
