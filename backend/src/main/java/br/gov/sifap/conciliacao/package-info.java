/**
 * Bounded context <b>conciliacao</b> — importação e conferência de retorno bancário
 * CNAB 240 do Banco do Brasil (substitui BATCHCON.NSN).
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "conciliacao",
        allowedDependencies = {"shared", "pagamentos"})
package br.gov.sifap.conciliacao;
