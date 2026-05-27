/**
 * Módulo compartilhado (kernel) do SIFAP 2.0 — utilitários sem regra de negócio.
 * Não pode importar nada de bounded contexts; só pode ser importado por eles.
 */
@org.springframework.modulith.ApplicationModule(displayName = "shared")
package br.gov.sifap.shared;
