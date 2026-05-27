package br.gov.sifap.beneficiarios.domain;

/**
 * Tipo de parentesco — espelha GRP-DEPENDENTE.PARENTESCO do DDM BENEFICIARIO.
 * <pre>
 *  FI = Filho   CO = Cônjuge   CJ = Cônjuge alternativo
 *  IR = Irmão   NT = Neto      TU = Tutelado     OU = Outro
 * </pre>
 */
public enum Parentesco { FI, CO, CJ, IR, NT, TU, OU }
