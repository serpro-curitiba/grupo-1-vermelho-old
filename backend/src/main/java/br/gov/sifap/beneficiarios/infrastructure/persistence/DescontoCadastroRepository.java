package br.gov.sifap.beneficiarios.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DescontoCadastroRepository extends JpaRepository<DescontoCadastroJpaEntity, UUID> {
    List<DescontoCadastroJpaEntity> findByBeneficiarioId(UUID beneficiarioId);
}
