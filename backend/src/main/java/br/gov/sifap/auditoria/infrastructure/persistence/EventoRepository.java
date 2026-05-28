package br.gov.sifap.auditoria.infrastructure.persistence;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventoRepository extends JpaRepository<EventoJpaEntity, UUID> {

    @Query("""
            SELECT e FROM EventoJpaEntity e
            WHERE e.ocorridoEm >= COALESCE(:de, e.ocorridoEm)
              AND e.ocorridoEm <= COALESCE(:ate, e.ocorridoEm)
              AND e.acao = COALESCE(:acao, e.acao)
              AND e.usuarioId = COALESCE(:usuarioId, e.usuarioId)
              AND e.agregado = COALESCE(:agregado, e.agregado)
              AND e.agregadoId = COALESCE(:agregadoId, e.agregadoId)
            ORDER BY e.ocorridoEm DESC
            """)
    List<EventoJpaEntity> filtrar(@Param("de") OffsetDateTime de,
                                  @Param("ate") OffsetDateTime ate,
                                  @Param("acao") String acao,
                                  @Param("usuarioId") String usuarioId,
                                  @Param("agregado") String agregado,
                                  @Param("agregadoId") String agregadoId);
}
