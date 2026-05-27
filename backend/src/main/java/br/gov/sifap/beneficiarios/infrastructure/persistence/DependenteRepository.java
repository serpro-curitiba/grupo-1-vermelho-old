package br.gov.sifap.beneficiarios.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DependenteRepository extends JpaRepository<DependenteJpaEntity, UUID> {
    List<DependenteJpaEntity> findByBeneficiarioIdOrderByNome(UUID beneficiarioId);
    long countByBeneficiarioIdAndSituacao(UUID beneficiarioId, String situacao);
    boolean existsByBeneficiarioIdAndCpf(UUID beneficiarioId, String cpf);
}
