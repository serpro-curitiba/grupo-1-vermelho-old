package br.gov.sifap.pagamentos.application;

import br.gov.sifap.pagamentos.domain.BeneficiarioSnapshot;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Adapter de produção do {@link BeneficiariosPort}.
 *
 * <p>Lê diretamente das tabelas do módulo {@code beneficiarios} e {@code programas}
 * por SQL nativo — abordagem "Shared DB / Independent Modules" prevista
 * pelo ADR-0002 para o MVP. Em uma evolução o adapter será substituído por
 * chamada à API in-process exposta via Spring Modulith Named Interface.</p>
 */
@Component
@Primary
@Profile("!test-empty")
public class BeneficiariosJdbcAdapter implements BeneficiariosPort {

    @PersistenceContext
    private EntityManager em;

    @Override
    @SuppressWarnings("unchecked")
    public List<BeneficiarioSnapshot> listarAtivosOrdenadosPorCpf() {
        List<Object[]> rows = em.createNativeQuery("""
            SELECT b.id, b.cpf, b.nome, b.regiao,
                   COALESCE((SELECT COUNT(*) FROM beneficiarios.dependente d
                              WHERE d.beneficiario_id = b.id AND d.situacao = 'ATIVO'),0) AS qtd_deps,
                   b.renda_mensal, b.idade, b.status, b.programa_codigo
              FROM beneficiarios.beneficiario b
             WHERE b.status = 'ATIVO'
             ORDER BY b.cpf
        """).getResultList();

        OffsetDateTime agora = OffsetDateTime.now();
        return rows.stream().map(r -> new BeneficiarioSnapshot(
                (UUID) r[0],
                (String) r[1],
                (String) r[2],
                ((Number) r[3]).intValue(),
                ((Number) r[4]).intValue(),
                (BigDecimal) r[5],
                ((Number) r[6]).intValue(),
                (String) r[7],
                (String) r[8],
                agora)).toList();
    }

    @Override
    public BigDecimal valorBaseDoProgramaAtivo(String programaCodigo) {
        Object v = em.createNativeQuery(
                "SELECT COALESCE(vlr_calc_ajustado, valor_base) FROM programas.programa WHERE codigo = ?1")
                .setParameter(1, programaCodigo)
                .getSingleResult();
        return v == null ? BigDecimal.ZERO : (BigDecimal) v;
    }
}
