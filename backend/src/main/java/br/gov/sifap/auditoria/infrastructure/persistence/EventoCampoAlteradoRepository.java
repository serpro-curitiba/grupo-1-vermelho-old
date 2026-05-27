package br.gov.sifap.auditoria.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoCampoAlteradoRepository extends JpaRepository<EventoCampoAlteradoJpaEntity, UUID> {
    List<EventoCampoAlteradoJpaEntity> findByEventoIdOrderByOrdem(UUID eventoId);
}
