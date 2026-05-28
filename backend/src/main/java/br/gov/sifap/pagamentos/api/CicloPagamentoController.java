package br.gov.sifap.pagamentos.api;

import br.gov.sifap.pagamentos.application.ConsultarResumoCicloUseCase;
import br.gov.sifap.pagamentos.application.GerarCicloPagamentoUseCase;
import br.gov.sifap.pagamentos.domain.Competencia;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoint REST que substitui o disparo batch noturno do legado (BATCHPGT.NSN).
 * Atende REQ-PAY-001..003.
 */
@RestController
@RequestMapping("/api/v1/ciclos")
public class CicloPagamentoController {

    private final GerarCicloPagamentoUseCase useCase;
        private final ConsultarResumoCicloUseCase resumoUseCase;

        public CicloPagamentoController(GerarCicloPagamentoUseCase useCase,
                                                                        ConsultarResumoCicloUseCase resumoUseCase) {
        this.useCase = useCase;
                this.resumoUseCase = resumoUseCase;
    }

    @PostMapping
    public ResponseEntity<GerarCicloResponse> gerar(@Valid @RequestBody GerarCicloRequest req) {
        // Em produção o requisitante virá do SecurityContext (REQ-PAY-050).
        String requisitanteId = "sistema-dev";
        var resultado = useCase.executar(new Competencia(req.competencia()), requisitanteId);
        var body = new GerarCicloResponse(
                resultado.cicloId(),
                resultado.status().name(),
                resultado.totalPagamentos(),
                resultado.valorTotal());
        return ResponseEntity.accepted()
                .location(URI.create("/api/v1/ciclos/" + resultado.cicloId()))
                .body(body);
    }

        @GetMapping("/{cicloId}/resumo")
        public ResumoCicloResponse resumo(@PathVariable UUID cicloId) {
                var r = resumoUseCase.executar(cicloId);
                return new ResumoCicloResponse(
                                r.cicloId(),
                                r.competencia(),
                                r.status(),
                                r.totalCandidatos(),
                                r.totalPagamentos(),
                                r.totalIgnorados(),
                                r.totalRejeitados(),
                                r.valorTotal(),
                                r.totalBruto(),
                                r.totalDesconto(),
                                r.totalLiquido(),
                                r.totalAbono(),
                                r.total13());
        }

    /** Request DTO com Bean Validation. */
    public record GerarCicloRequest(
            @NotBlank
            @Pattern(regexp = "^\\d{6}$", message = "competencia.formato-invalido")
            String competencia) {}

    /** Response DTO. */
    public record GerarCicloResponse(
            UUID cicloId,
            String status,
            int totalPagamentos,
            BigDecimal valorTotal) {}

    public record ResumoCicloResponse(
            UUID cicloId,
            String competencia,
            String status,
            int totalCandidatos,
            int totalPagamentos,
            int totalIgnorados,
            int totalRejeitados,
            BigDecimal valorTotal,
            BigDecimal totalBruto,
            BigDecimal totalDesconto,
            BigDecimal totalLiquido,
            BigDecimal totalAbono,
            BigDecimal total13) {}
}
