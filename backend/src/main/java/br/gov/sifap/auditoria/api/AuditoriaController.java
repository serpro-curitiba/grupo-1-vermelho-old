package br.gov.sifap.auditoria.api;

import br.gov.sifap.auditoria.infrastructure.persistence.EventoCampoAlteradoRepository;
import br.gov.sifap.auditoria.infrastructure.persistence.EventoRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

/** Consulta de trilha de auditoria. Espelha RELAUDIT.NSN (RPT-014). */
@RestController
@RequestMapping("/api/v1/auditoria")
public class AuditoriaController {

    private final EventoRepository eventoRepo;
    private final EventoCampoAlteradoRepository campoRepo;

    public AuditoriaController(EventoRepository e, EventoCampoAlteradoRepository c) {
        this.eventoRepo = e; this.campoRepo = c;
    }

    @GetMapping("/eventos")
    public List<EventoResponse> consultar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime ate,
            @RequestParam(required = false) String acao,
            @RequestParam(required = false) String usuarioId,
            @RequestParam(required = false) String agregado,
            @RequestParam(required = false) String agregadoId) {
        return eventoRepo.filtrar(de, ate, acao, usuarioId, agregado, agregadoId).stream()
                .map(e -> new EventoResponse(e.getId(), e.getOcorridoEm().toString(),
                        e.getAcao(), e.getTipo(), e.getAgregado(), e.getAgregadoId(),
                        e.getUsuarioId(), e.getIpOrigem(), e.getIdCorrelacao(),
                        "S".equals(e.getSucesso())))
                .toList();
    }

    @GetMapping("/eventos/{id}/campos")
    public List<CampoResponse> camposDoEvento(@PathVariable UUID id) {
        return campoRepo.findByEventoIdOrderByOrdem(id).stream()
                .map(c -> new CampoResponse(c.getOrdem(), c.getCampo(), c.getValorAnt(), c.getValorPos()))
                .toList();
    }

    public record EventoResponse(UUID id, String ocorridoEm, String acao, String tipo,
                                 String agregado, String agregadoId, String usuarioId,
                                 String ipOrigem, String idCorrelacao, boolean sucesso) {}
    public record CampoResponse(int ordem, String campo, String valorAnt, String valorPos) {}
}
