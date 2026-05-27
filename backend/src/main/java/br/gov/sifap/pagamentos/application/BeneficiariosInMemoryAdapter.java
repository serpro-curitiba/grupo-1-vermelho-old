package br.gov.sifap.pagamentos.application;

import br.gov.sifap.pagamentos.domain.BeneficiarioSnapshot;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Adapter de demonstração para o MVP — mantém uma lista em memória.
 * É substituído em produção por integração com o módulo {@code beneficiarios}.
 */
@Component
@Profile("inmemory")
public class BeneficiariosInMemoryAdapter implements BeneficiariosPort {

    private final List<BeneficiarioSnapshot> base = new ArrayList<>();

    public BeneficiariosInMemoryAdapter() {
        // Seed de 5 beneficiários: 4 ativos (CPFs diferentes) + 1 suspenso.
        base.add(snap("11111111111", "Ana", 1, 2, "1500.00", 70, "ATIVO"));
        base.add(snap("22222222222", "Bruno", 7, 0, "4500.00", 35, "ATIVO"));
        base.add(snap("33333333333", "Carla", 12, 4, "1000.00", 28, "ATIVO"));
        base.add(snap("44444444444", "Diego", 25, 1, "2800.00", 50, "ATIVO"));
        base.add(snap("55555555555", "Eva",  3, 0, "1200.00", 42, "SUSPENSO"));
    }

    private BeneficiarioSnapshot snap(String cpf, String nome, int reg, int deps,
                                      String renda, int idade, String status) {
        return new BeneficiarioSnapshot(
                UUID.nameUUIDFromBytes(cpf.getBytes()),
                cpf, nome, reg, deps, new BigDecimal(renda), idade, status,
                "BOLSA-FAMILIA", OffsetDateTime.now());
    }

    @Override
    public List<BeneficiarioSnapshot> listarAtivosOrdenadosPorCpf() {
        return base.stream()
                .filter(b -> "ATIVO".equals(b.statusOrigem()))
                .sorted(Comparator.comparing(BeneficiarioSnapshot::cpf))
                .toList();
    }

    @Override
    public BigDecimal valorBaseDoProgramaAtivo(String programaCodigo) {
        // MVP — valor fixo do programa BOLSA-FAMILIA.
        return new BigDecimal("600.00");
    }
}
