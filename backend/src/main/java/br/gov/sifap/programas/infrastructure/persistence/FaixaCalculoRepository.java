package br.gov.sifap.programas.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FaixaCalculoRepository extends JpaRepository<FaixaCalculoJpaEntity, UUID> {
    List<FaixaCalculoJpaEntity> findByProgramaCodigoOrderByOrdem(String programaCodigo);
    void deleteByProgramaCodigo(String programaCodigo);
}
