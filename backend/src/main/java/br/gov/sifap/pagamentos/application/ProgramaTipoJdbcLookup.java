package br.gov.sifap.pagamentos.application;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

/** Lookup do tipo do programa social — usado por BATCHPGT p/ decidir 13º/abono. */
@Component
public class ProgramaTipoJdbcLookup {

    @PersistenceContext
    private EntityManager em;

    public String tipo(String programaCodigo) {
        var rows = em.createNativeQuery("SELECT tipo FROM programas.programa WHERE codigo = ?1")
                .setParameter(1, programaCodigo)
                .getResultList();
        if (rows.isEmpty() || rows.get(0) == null) {
            return "A";
        }
        return rows.get(0).toString();
    }
}
