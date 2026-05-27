package br.gov.sifap.beneficiarios.domain;

/** Estado civil — espelha BENEFICIARIO-V.EST-CIVIL. S/C/D/V/U. */
public enum EstadoCivil {
    SOLTEIRO("S"), CASADO("C"), DIVORCIADO("D"), VIUVO("V"), UNIAO_ESTAVEL("U");

    private final String codigoLegado;
    EstadoCivil(String c) { this.codigoLegado = c; }
    public String codigoLegado() { return codigoLegado; }
}
