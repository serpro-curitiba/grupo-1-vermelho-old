package br.gov.sifap.pagamentos.domain;

/** Estados possíveis do agregado {@link CicloPagamento}. */
public enum StatusCiclo {
    INICIADO,
    CALCULANDO,
    CALCULADO,
    EMITIDO,
    CANCELADO
}
