package br.gov.sifap.programas.domain;

/** Tipo de programa social — espelha PROGRAMA-SOCIAL.TIPO-PROGRAMA. */
public enum TipoPrograma {
    ASSISTENCIAL("A"), PREVIDENCIARIO("P"), TRABALHO("T");
    private final String codigoLegado;
    TipoPrograma(String c) { this.codigoLegado = c; }
    public String codigoLegado() { return codigoLegado; }
    public static TipoPrograma doLegado(String c) {
        return switch (c) {
            case "A" -> ASSISTENCIAL; case "P" -> PREVIDENCIARIO; case "T" -> TRABALHO;
            default -> throw new IllegalArgumentException("tipo desconhecido: " + c);
        };
    }
}
