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
        Object v = em.createNativeQuery("SELECT tipo FROM programas.programa WHERE codigo = ?1")
                .setParameter(1, programaCodigo).getResultStream().findFirst().orElse(null);
        return v == null ? "A" : (String) v;
    }
}
