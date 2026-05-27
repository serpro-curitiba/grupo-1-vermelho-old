package br.gov.sifap.pagamentos.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CicloPagamentoRepository extends JpaRepository<CicloPagamentoJpaEntity, UUID> {

    /**
     * Verifica a existência de um ciclo NÃO cancelado para uma competência.
     * Suporta REQ-PAY-002 (bloqueio de duplicidade).
     */
    Optional<CicloPagamentoJpaEntity> findFirstByCompetenciaAndStatusNot(String competencia, String status);
}
