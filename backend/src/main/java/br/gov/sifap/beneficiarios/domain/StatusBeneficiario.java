package br.gov.sifap.beneficiarios.domain;

/** Status do beneficiário — espelha BENEFICIARIO-V.SITUACAO ('A'/'S'/'C'/'I'/'D'). */
public enum StatusBeneficiario {
    ATIVO("A"),
    SUSPENSO("S"),
    CANCELADO("C"),
    INATIVO("I"),
    DESLIGADO("D");

    private final String codigoLegado;

    StatusBeneficiario(String codigoLegado) { this.codigoLegado = codigoLegado; }
    public String codigoLegado() { return codigoLegado; }

    public static StatusBeneficiario doLegado(String c) {
        if (c == null) return ATIVO;
        return switch (c) {
            case "A" -> ATIVO;
            case "S" -> SUSPENSO;
            case "C" -> CANCELADO;
            case "I" -> INATIVO;
            case "D" -> DESLIGADO;
            default  -> throw new IllegalArgumentException("status legado desconhecido: " + c);
        };
    }
}
