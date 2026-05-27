/**
 * Bounded context <b>relatorios</b> — saídas consolidadas (BATCHREL), analíticas
 * (RELPGT) e trilha de auditoria (RELAUDIT). Geração em CSV/JSON.
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "relatorios",
        allowedDependencies = {"shared", "pagamentos", "auditoria"})
package br.gov.sifap.relatorios;
