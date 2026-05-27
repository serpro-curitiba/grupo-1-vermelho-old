package br.gov.sifap.pagamentos.api;

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

    public CicloPagamentoController(GerarCicloPagamentoUseCase useCase) {
        this.useCase = useCase;
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

    /** Request DTO com Bean Validation. */
    public record GerarCicloRequest(
            @NotBlank
            @Pattern(regexp = "^[0-9]{6}$", message = "competencia.formato-invalido")
            String competencia) {}

    /** Response DTO. */
    public record GerarCicloResponse(
            UUID cicloId,
            String status,
            int totalPagamentos,
            BigDecimal valorTotal) {}
}
