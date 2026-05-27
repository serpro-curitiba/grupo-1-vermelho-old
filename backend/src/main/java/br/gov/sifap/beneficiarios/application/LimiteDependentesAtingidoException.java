package br.gov.sifap.beneficiarios.application;

import java.io.Serial;

public class LimiteDependentesAtingidoException extends RuntimeException {
    @Serial private static final long serialVersionUID = 1L;
    public LimiteDependentesAtingidoException() { super("dependentes.limite-atingido"); }
}
