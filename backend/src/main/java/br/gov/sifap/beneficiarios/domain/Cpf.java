package br.gov.sifap.beneficiarios.domain;

import br.gov.sifap.shared.CpfValidator;
import java.util.Objects;

/** Value Object CPF — imutável, valida na construção via {@link CpfValidator}. */
public record Cpf(String numero) {

    public Cpf {
        Objects.requireNonNull(numero, "cpf");
        if (!CpfValidator.valido(numero)) {
            throw new IllegalArgumentException("cpf-invalido");
        }
    }

    public static Cpf de(String raw) {
        return new Cpf(raw == null ? null : raw.replaceAll("\\D", ""));
    }
}
