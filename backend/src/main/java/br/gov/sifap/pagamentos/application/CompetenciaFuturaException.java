package br.gov.sifap.pagamentos.application;

/** Lançada quando a competência informada é posterior à corrente (REQ-PAY-003). */
public class CompetenciaFuturaException extends RuntimeException {

    public CompetenciaFuturaException(String competencia) {
        super("competencia.futura: " + competencia);
    }
}
