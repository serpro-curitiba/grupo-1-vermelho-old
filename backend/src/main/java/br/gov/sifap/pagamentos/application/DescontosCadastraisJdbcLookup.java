package br.gov.sifap.pagamentos.application;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Lê descontos cadastrais (CALCDSCT) via SQL nativo. */
@Component
public class DescontosCadastraisJdbcLookup {

    @PersistenceContext
    private EntityManager em;

    public record CadastroDesconto(String tipo, BigDecimal valorFixo, BigDecimal percentual) {}

    @SuppressWarnings("unchecked")
    public List<CadastroDesconto> doBeneficiario(UUID beneficiarioId) {
        List<Object[]> rows = em.createNativeQuery("""
            SELECT tipo, valor_fixo, percentual
              FROM beneficiarios.desconto_cadastro
             WHERE beneficiario_id = ?1
               AND (dt_fim IS NULL OR dt_fim >= current_date)
               AND dt_inicio <= current_date
        """).setParameter(1, beneficiarioId).getResultList();

        var out = new ArrayList<CadastroDesconto>();
        for (Object[] r : rows) {
            out.add(new CadastroDesconto((String) r[0], (BigDecimal) r[1], (BigDecimal) r[2]));
        }
        return out;
    }
}
