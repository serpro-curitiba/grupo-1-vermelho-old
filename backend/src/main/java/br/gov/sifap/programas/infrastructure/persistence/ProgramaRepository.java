package br.gov.sifap.programas.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramaRepository extends JpaRepository<ProgramaJpaEntity, String> {
    List<ProgramaJpaEntity> findByStatus(String status);
    Optional<ProgramaJpaEntity> findBySigla(String sigla);
}
