package br.gov.sifap;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * Garante ADR-0002 — pacote por bounded context, sem dependências indevidas.
 * Falha o build se algum módulo (ex.: {@code pagamentos}) passar a depender de outro
 * fora do que está declarado em {@code package-info.java}.
 */
class ModulithVerificationTest {

    @Test
    void verifyModules() {
        ApplicationModules.of(SifapApplication.class).verify();
    }
}
