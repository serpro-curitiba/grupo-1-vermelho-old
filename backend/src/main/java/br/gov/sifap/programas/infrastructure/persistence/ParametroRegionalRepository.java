package br.gov.sifap.programas.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParametroRegionalRepository extends JpaRepository<ParametroRegionalJpaEntity, UUID> {
    List<ParametroRegionalJpaEntity> findByProgramaCodigo(String programaCodigo);
}
