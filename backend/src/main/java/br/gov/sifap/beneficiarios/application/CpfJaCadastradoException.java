package br.gov.sifap.beneficiarios.application;

import java.io.Serial;

public class CpfJaCadastradoException extends RuntimeException {
    @Serial private static final long serialVersionUID = 1L;
    public CpfJaCadastradoException(String cpf) { super("cpf.ja-cadastrado: " + cpf.substring(0,3) + "***"); }
}
