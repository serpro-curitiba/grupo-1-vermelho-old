package br.gov.sifap.conciliacao.domain;

import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ParserCnab240Test {

    @Test @DisplayName("REQ-CON-001: ignora registros que não são tipo 3 (detalhe)")
    void ignoraNaoDetalhe() throws Exception {
        // header de arquivo (tipo 0)
        String l = pad("0010000", 240);
        var lista = ParserCnab240.ler(new ByteArrayInputStream(l.getBytes(StandardCharsets.ISO_8859_1)));
        assertTrue(lista.isEmpty());
    }

    @Test @DisplayName("REQ-CON-002: parser extrai CPF, valor, retorno e num_pagto nas posições corretas")
    void parsePosicoes() throws Exception {
        StringBuilder l = new StringBuilder(" ".repeat(240));
        sub(l, 0,   "001");                       // banco
        sub(l, 3,   "0001");                      // lote
        sub(l, 7,   "3");                         // tipo registro
        sub(l, 73,  "0000000123");                // num_pagto col 74-83
        sub(l, 43,  "11144477735");               // cpf col 44-54
        sub(l, 119, "000000000050000");           // valor 500,00 com 2 dec implícitas
        sub(l, 139, "31122024");                  // data 31/12/2024
        sub(l, 230, "00");                        // cod retorno = PAGO

        var lista = ParserCnab240.ler(new ByteArrayInputStream(l.toString().getBytes(StandardCharsets.ISO_8859_1)));
        assertEquals(1, lista.size());
        var r = lista.get(0);
        assertEquals("001", r.banco());
        assertEquals("11144477735", r.cpf());
        assertEquals(123L, r.numPagto());
        assertEquals(0, r.valorRetorno().compareTo(new java.math.BigDecimal("500.00")));
        assertEquals("00", r.codRetorno());
        assertEquals(CodigoRetorno.PAGO, CodigoRetorno.doLegado(r.codRetorno()));
    }

    private static void sub(StringBuilder b, int pos, String v) {
        b.replace(pos, pos + v.length(), v);
    }

    private static String pad(String s, int n) {
        if (s.length() >= n) return s.substring(0, n);
        return s + " ".repeat(n - s.length());
    }
}
