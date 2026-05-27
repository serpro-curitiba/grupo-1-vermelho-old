package br.gov.sifap.shared;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CpfValidatorTest {

    @Test @DisplayName("REQ-BEN-001: CPF válido conhecido passa mod-11")
    void cpfValido() {
        assertTrue(CpfValidator.valido("11144477735"));
    }

    @Test @DisplayName("REQ-BEN-002: CPF com 11 dígitos iguais é rejeitado")
    void todosIguais() {
        assertFalse(CpfValidator.valido("11111111111"));
    }

    @Test @DisplayName("REQ-BEN-003: VALBENEF — prefixo 000 bypassa regra de dígitos iguais")
    void prefixo000() {
        assertTrue(CpfValidator.valido("00000000000"));
    }

    @Test @DisplayName("REQ-BEN-004: CPF inválido (dv errado) é rejeitado")
    void dvErrado() {
        assertFalse(CpfValidator.valido("11144477700"));
    }

    @Test @DisplayName("REQ-BEN-005: tamanho diferente de 11 falha")
    void tamanhoErrado() {
        assertFalse(CpfValidator.valido("123"));
        assertFalse(CpfValidator.valido(""));
        assertFalse(CpfValidator.valido(null));
    }
}
