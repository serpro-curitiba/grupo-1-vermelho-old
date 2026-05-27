package br.gov.sifap.beneficiarios.application;

import java.io.Serial;

public class BeneficiarioNaoEncontradoException extends RuntimeException {
    @Serial private static final long serialVersionUID = 1L;
    public BeneficiarioNaoEncontradoException(String chave) { super("beneficiario.nao-encontrado: " + chave); }
}
