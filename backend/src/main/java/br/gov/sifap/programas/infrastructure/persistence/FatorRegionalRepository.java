package br.gov.sifap.programas.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FatorRegionalRepository extends JpaRepository<FatorRegionalJpaEntity, Integer> {
    @Override List<FatorRegionalJpaEntity> findAll();
}
