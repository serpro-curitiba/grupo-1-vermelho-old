/**
 * Bounded context <b>pagamentos</b> — geração de ciclo, cálculo e emissão de pagamentos
 * (substitui {@code BATCHPGT.NSN}).
 *
 * <p>Cobre os requisitos REQ-PAY-001..030.</p>
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "pagamentos",
        allowedDependencies = {"shared"})
package br.gov.sifap.pagamentos;
