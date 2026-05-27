package br.gov.sifap.pagamentos.domain;

/**
 * Tipo de desconto — espelha CALCDSCT.NSN (DESCONTOS-V[*].TIPO):
 * <pre>
 *   C = Contribuição social    (tabela progressiva por faixa de renda)
 *   I = Imposto de renda
 *   J = Judicial               (NÃO sofre teto de 30%)
 *   S = Sindical                (1% fixo)
 *   P = Pensão alimentícia
 *   A = Administrativo
 * </pre>
 */
public enum TipoDesconto {
    CONTRIBUICAO("C"), IMPOSTO("I"), JUDICIAL("J"),
    SINDICAL("S"), PENSAO("P"), ADMINISTRATIVO("A");

    private final String codigoLegado;
    TipoDesconto(String c) { this.codigoLegado = c; }
    public String codigoLegado() { return codigoLegado; }
    public static TipoDesconto doLegado(String c) {
        return switch (c) {
            case "C" -> CONTRIBUICAO; case "I" -> IMPOSTO; case "J" -> JUDICIAL;
            case "S" -> SINDICAL; case "P" -> PENSAO; case "A" -> ADMINISTRATIVO;
            default -> throw new IllegalArgumentException("tipo desconto legado: " + c);
        };
    }
}
