/**
 * Bounded context <b>auditoria</b> — registro append-only de eventos de negócio
 * com retenção mínima de 10 anos. Espelha o DDM AUDITORIA (ARQ 153) e RELAUDIT.
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "auditoria",
        allowedDependencies = {"shared"})
package br.gov.sifap.auditoria;
