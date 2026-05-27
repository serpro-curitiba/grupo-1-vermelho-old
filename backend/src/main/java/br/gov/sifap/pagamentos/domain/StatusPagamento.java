package br.gov.sifap.pagamentos.domain;

/** Estados possíveis de um {@link Pagamento} individual. */
public enum StatusPagamento {
    PENDENTE,
    CALCULADO,
    EMITIDO,
    PAGO,
    REJEITADO,
    CANCELADO
}
