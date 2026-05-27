package br.gov.sifap.beneficiarios.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeneficiarioRepository extends JpaRepository<BeneficiarioJpaEntity, UUID> {
    Optional<BeneficiarioJpaEntity> findByCpf(String cpf);
    Optional<BeneficiarioJpaEntity> findByNis(String nis);
    Optional<BeneficiarioJpaEntity> findByNumInscricao(Long numInscricao);
    List<BeneficiarioJpaEntity> findByStatusOrderByCpf(String status);
    List<BeneficiarioJpaEntity> findByProgramaCodigoAndStatusOrderByCpf(String programaCodigo, String status);
}
