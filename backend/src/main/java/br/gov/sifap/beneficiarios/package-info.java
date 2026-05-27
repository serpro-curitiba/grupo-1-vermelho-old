/**
 * Bounded context <b>beneficiarios</b> — cadastro, validação, dependentes
 * e consulta. Substitui CADBENEF, CADDEPEND, VALBENEF, VALDOCS, CONSBENF.
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "beneficiarios",
        allowedDependencies = {"shared"})
package br.gov.sifap.beneficiarios;
