package br.gov.sifap.relatorios.api;

import br.gov.sifap.relatorios.application.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

/** Endpoints REST de relatórios. */
@RestController
@RequestMapping("/api/v1/relatorios")
public class RelatoriosController {

    private final RelatorioConsolidadoUseCase consolidado;
    private final RelatorioAnaliticoUseCase analitico;
    private final TrilhaAuditoriaUseCase trilha;

    public RelatoriosController(RelatorioConsolidadoUseCase c, RelatorioAnaliticoUseCase a, TrilhaAuditoriaUseCase t) {
        this.consolidado = c; this.analitico = a; this.trilha = t;
    }

    @GetMapping("/consolidado")
    public List<RelatorioConsolidadoUseCase.LinhaConsolidada> consolidado(@RequestParam String competencia) {
        return consolidado.executar(competencia);
    }

    @GetMapping("/analitico")
    public List<RelatorioAnaliticoUseCase.LinhaAnalitica> analitico(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @RequestParam(required = false) String codPrograma) {
        return analitico.executar(de, ate, codPrograma);
    }

    @GetMapping("/auditoria")
    public List<TrilhaResponse> auditoria(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime ate,
            @RequestParam(required = false) String acao,
            @RequestParam(required = false) String usuarioId,
            @RequestParam(required = false) String agregado,
            @RequestParam(required = false) String agregadoId,
            @RequestParam(defaultValue = "false") boolean incluirExclusoes) {
        return trilha.executar(de, ate, acao, usuarioId, agregado, agregadoId, incluirExclusoes).stream()
                .map(e -> new TrilhaResponse(e.getOcorridoEm().toString(), e.getAcao(),
                        e.getTipo(), e.getAgregado(), e.getAgregadoId(),
                        e.getUsuarioId(), "S".equals(e.getSucesso())))
                .toList();
    }

    public record TrilhaResponse(String ocorridoEm, String acao, String tipo,
                                 String agregado, String agregadoId, String usuarioId, boolean sucesso) {}
}
