package br.gov.sifap.conciliacao.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Registro de detalhe (segmento) do arquivo CNAB 240 BB.
 *
 * <p>Posições paritárias ao BATCHCON.NSN:</p>
 * <pre>
 *  banco        col 001-003 (3 dígitos)
 *  lote         col 004-007 (4 dígitos)
 *  tipo-reg     col 008     (3 = detalhe)
 *  num-doc      col 074-083 (10 dígitos = num_pagto)
 *  cpf          col 044-054 (11 dígitos)
 *  vlr-retorno  col 120-134 (15 dígitos, 2 decimais implícitas)
 *  dt-pagto     col 140-147 (DDMMYYYY)
 *  cod-retorno  col 231-232 (2 dígitos)
 * </pre>
 */
public record RegistroCnab240(
        String banco,
        String lote,
        String cpf,
        Long numPagto,
        BigDecimal valorRetorno,
        LocalDate dtPagamento,
        String codRetorno) {
}
