package br.gov.sifap.conciliacao.domain;

/**
 * Códigos de retorno do CNAB 240 do Banco do Brasil — paridade BATCHCON.NSN.
 * <pre>
 *  00 → PAGO          (PA)
 *  01 → DEVOLVIDO     (DV)
 *  02 → ESTORNADO     (ES)
 *  outros → REJEITADO (RJ)
 * </pre>
 */
public enum CodigoRetorno {
    PAGO("00"), DEVOLVIDO("01"), ESTORNADO("02"), REJEITADO("99");

    private final String legado;
    CodigoRetorno(String l) { this.legado = l; }
    public String codigo() { return legado; }

    public static CodigoRetorno doLegado(String c) {
        if (c == null) return REJEITADO;
        return switch (c.trim()) {
            case "00" -> PAGO;
            case "01" -> DEVOLVIDO;
            case "02" -> ESTORNADO;
            default   -> REJEITADO;
        };
    }
}
