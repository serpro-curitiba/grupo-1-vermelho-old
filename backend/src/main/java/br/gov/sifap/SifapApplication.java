package br.gov.sifap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;

/**
 * Entry-point do Modular Monolith SIFAP 2.0.
 *
 * <p>Atende ADR-0002: pacote-por-bounded-context, com Spring Modulith validando
 * que módulos só dependem das APIs explicitamente expostas.</p>
 */
@SpringBootApplication
@Modulithic(systemName = "SIFAP 2.0", sharedModules = "shared")
public class SifapApplication {

    public static void main(String[] args) {
        SpringApplication.run(SifapApplication.class, args);
    }
}
