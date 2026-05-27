package br.gov.sifap.pagamentos.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagamentoRepository extends JpaRepository<PagamentoJpaEntity, UUID> {
    List<PagamentoJpaEntity> findByCicloId(UUID cicloId);
    List<PagamentoJpaEntity> findByCpfOrderByDtGeracaoDesc(String cpf);
    java.util.Optional<PagamentoJpaEntity> findFirstByNumPagto(Long numPagto);
    java.util.Optional<PagamentoJpaEntity> findFirstByCpfAndCompetencia(String cpf, String competencia);
}
