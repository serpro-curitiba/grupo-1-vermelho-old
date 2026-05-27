package br.gov.sifap.programas.api;

import br.gov.sifap.programas.application.CalculadoraCorrecaoService;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import org.springframework.web.bind.annotation.*;

/** Endpoint REST para correção IPCA (CALCCORR.NSN). */
@RestController
@RequestMapping("/api/v1/correcao")
public class CorrecaoController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMM");

    private final CalculadoraCorrecaoService servico;

    public CorrecaoController(CalculadoraCorrecaoService s) { this.servico = s; }

    @GetMapping("/ipca")
    public CorrecaoResponse corrigir(@RequestParam @NotNull BigDecimal valor,
                                     @RequestParam String de,
                                     @RequestParam String ate) {
        YearMonth ymDe  = YearMonth.parse(de,  FMT);
        YearMonth ymAte = YearMonth.parse(ate, FMT);
        BigDecimal corrigido = servico.corrigir(valor, ymDe, ymAte);
        return new CorrecaoResponse(valor, corrigido, de, ate);
    }

    public record CorrecaoResponse(BigDecimal valorOriginal, BigDecimal valorCorrigido,
                                   String competenciaDe, String competenciaAte) {}
}
