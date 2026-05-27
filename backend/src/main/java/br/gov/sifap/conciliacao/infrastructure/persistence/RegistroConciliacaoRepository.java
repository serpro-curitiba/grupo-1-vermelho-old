package br.gov.sifap.conciliacao.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistroConciliacaoRepository extends JpaRepository<RegistroConciliacaoJpaEntity, UUID> {
    List<RegistroConciliacaoJpaEntity> findByArquivoId(UUID arquivoId);
}
