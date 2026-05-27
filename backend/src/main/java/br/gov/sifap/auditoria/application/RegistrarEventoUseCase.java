package br.gov.sifap.auditoria.application;

import br.gov.sifap.auditoria.domain.CodigoAcao;
import br.gov.sifap.auditoria.infrastructure.persistence.*;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Substitui as gravações implícitas em AUDITORIA do legado (chamadas embutidas
 * em CADBENEF, ALTBENEF, EXCBENEF, CADPROG, BATCHPGT, BATCHCON, BATCHREL).
 *
 * <p>Append-only: nunca expõe update/delete.</p>
 */
@Service
public class RegistrarEventoUseCase {

    private final EventoRepository eventoRepo;
    private final EventoCampoAlteradoRepository campoRepo;
    private final Clock clock;

    public RegistrarEventoUseCase(EventoRepository e, EventoCampoAlteradoRepository c, Clock clock) {
        this.eventoRepo = e; this.campoRepo = c; this.clock = clock;
    }

    @Transactional
    public UUID registrar(NovoEventoCommand cmd) {
        UUID id = UUID.randomUUID();
        eventoRepo.save(new EventoJpaEntity(
                id, cmd.tipo(), cmd.agregado(), cmd.agregadoId(), cmd.usuarioId(),
                cmd.payloadJson() == null ? "{}" : cmd.payloadJson(),
                OffsetDateTime.now(clock),
                cmd.acao() == null ? null : cmd.acao().name(),
                cmd.ipOrigem(), cmd.idCorrelacao(),
                cmd.sucesso() ? "S" : "N"));
        if (cmd.camposAlterados() != null) {
            int ordem = 1;
            for (CampoAlterado c : cmd.camposAlterados()) {
                if (ordem > 20) break; // limite MU=20 do DDM
                campoRepo.save(new EventoCampoAlteradoJpaEntity(
                        UUID.randomUUID(), id, ordem++, c.campo(), c.valorAnt(), c.valorPos()));
            }
        }
        return id;
    }

    public record NovoEventoCommand(String tipo, String agregado, String agregadoId,
                                    String usuarioId, String payloadJson,
                                    CodigoAcao acao, String ipOrigem, String idCorrelacao,
                                    boolean sucesso, List<CampoAlterado> camposAlterados) {}

    public record CampoAlterado(String campo, String valorAnt, String valorPos) {}
}
