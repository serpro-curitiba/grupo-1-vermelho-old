package br.gov.sifap.pagamentos.application;

import java.util.UUID;

public class CicloNaoEncontradoException extends RuntimeException {

    public CicloNaoEncontradoException(UUID cicloId) {
        super("ciclo.nao-encontrado: não existe ciclo para o id " + cicloId);
    }
}