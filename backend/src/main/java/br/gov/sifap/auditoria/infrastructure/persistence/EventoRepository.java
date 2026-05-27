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
            WHERE (:de IS NULL OR e.ocorridoEm >= :de)
              AND (:ate IS NULL OR e.ocorridoEm <= :ate)
              AND (:acao IS NULL OR e.acao = :acao)
              AND (:usuarioId IS NULL OR e.usuarioId = :usuarioId)
              AND (:agregado IS NULL OR e.agregado = :agregado)
            ORDER BY e.ocorridoEm DESC
            """)
    List<EventoJpaEntity> filtrar(@Param("de") OffsetDateTime de,
                                  @Param("ate") OffsetDateTime ate,
                                  @Param("acao") String acao,
                                  @Param("usuarioId") String usuarioId,
                                  @Param("agregado") String agregado);
}
