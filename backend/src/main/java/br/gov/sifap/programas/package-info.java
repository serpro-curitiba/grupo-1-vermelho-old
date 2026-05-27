/**
 * Bounded context <b>programas</b> — cadastro de programas sociais com
 * FATOR-K (CADPROG), faixas de cálculo, parâmetros regionais e tabela IPCA.
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "programas",
        allowedDependencies = {"shared"})
package br.gov.sifap.programas;
