package br.gov.sifap.pagamentos.infrastructure.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEventJpaEntity, UUID> {
}
