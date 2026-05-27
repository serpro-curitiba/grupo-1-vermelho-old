package br.gov.sifap.pagamentos.application;

/** Lançada quando já existe um ciclo não-cancelado para a competência (REQ-PAY-002). */
public class CicloDuplicadoException extends RuntimeException {

    private final String competencia;

    public CicloDuplicadoException(String competencia) {
        super("ciclo.duplicado: já existe ciclo para a competência " + competencia);
        this.competencia = competencia;
    }

    public String getCompetencia() { return competencia; }
}
