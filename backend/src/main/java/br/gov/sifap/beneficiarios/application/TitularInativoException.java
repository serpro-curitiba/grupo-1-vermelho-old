package br.gov.sifap.beneficiarios.application;

import java.io.Serial;

public class TitularInativoException extends RuntimeException {
    @Serial private static final long serialVersionUID = 1L;
    public TitularInativoException(String status) { super("titular.inativo: " + status); }
}
