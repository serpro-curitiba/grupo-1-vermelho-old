package br.gov.sifap.programas.api;

import br.gov.sifap.programas.application.CadastrarProgramaUseCase;
import br.gov.sifap.programas.infrastructure.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** API REST do bounded context programas. */
@RestController
@RequestMapping("/api/v1/programas")
public class ProgramaController {

    private final CadastrarProgramaUseCase cadastrar;
    private final ProgramaRepository repo;
    private final FaixaCalculoRepository faixaRepo;
    private final ParametroRegionalRepository paramRepo;

    public ProgramaController(CadastrarProgramaUseCase c, ProgramaRepository r,
                              FaixaCalculoRepository f, ParametroRegionalRepository p) {
        this.cadastrar = c; this.repo = r; this.faixaRepo = f; this.paramRepo = p;
    }

    @PostMapping
    public ResponseEntity<ProgramaResponse> cadastrar(@Valid @RequestBody ProgramaRequest req) {
        var cmd = new CadastrarProgramaUseCase.NovoProgramaCommand(
                req.codigo(), req.nome(), req.sigla(), req.valorBase(), req.tipo(),
                req.codElegibilidade(), req.dtInicio(), req.dtFim(),
                req.rendaMax(), req.idadeMin(), req.idadeMax(), req.pctReajusteAnual(),
                req.faixas() == null ? List.of() :
                    req.faixas().stream().map(f ->
                        new CadastrarProgramaUseCase.FaixaInput(f.ordem(), f.rendaAte(), f.fator())).toList(),
                req.parametrosRegionais() == null ? List.of() :
                    req.parametrosRegionais().stream().map(p ->
                        new CadastrarProgramaUseCase.ParamRegionalInput(p.regiao(), p.fatorExtra())).toList());
        var saved = cadastrar.executar(cmd);
        return ResponseEntity.created(URI.create("/api/v1/programas/" + saved.getCodigo()))
                .body(ProgramaResponse.de(saved));
    }

    @GetMapping("/{codigo}")
    public ProgramaResponse get(@PathVariable String codigo) {
        return repo.findById(codigo).map(ProgramaResponse::de)
                .orElseThrow(() -> new IllegalArgumentException("programa.nao-encontrado: " + codigo));
    }

    @GetMapping
    public List<ProgramaResponse> listar() {
        return repo.findAll().stream().map(ProgramaResponse::de).toList();
    }

    @GetMapping("/{codigo}/faixas")
    public List<FaixaResponse> faixas(@PathVariable String codigo) {
        return faixaRepo.findByProgramaCodigoOrderByOrdem(codigo).stream()
                .map(f -> new FaixaResponse(f.getOrdem(), f.getRendaAte(), f.getFator())).toList();
    }

    @GetMapping("/{codigo}/parametros-regionais")
    public List<ParamRegionalResponse> params(@PathVariable String codigo) {
        return paramRepo.findByProgramaCodigo(codigo).stream()
                .map(p -> new ParamRegionalResponse(p.getRegiao(), p.getFatorExtra())).toList();
    }

    public record ProgramaRequest(
            @NotBlank String codigo, @NotBlank String nome, String sigla,
            @NotNull BigDecimal valorBase, @NotBlank String tipo,
            String codElegibilidade, LocalDate dtInicio, LocalDate dtFim,
            BigDecimal rendaMax, Integer idadeMin, Integer idadeMax,
            BigDecimal pctReajusteAnual,
            List<FaixaInput> faixas, List<ParamRegionalInput> parametrosRegionais) {}
    public record FaixaInput(int ordem, BigDecimal rendaAte, BigDecimal fator) {}
    public record ParamRegionalInput(int regiao, BigDecimal fatorExtra) {}

    public record ProgramaResponse(String codigo, String nome, String sigla, String status,
                                   String tipo, BigDecimal valorBase, BigDecimal fatorK,
                                   BigDecimal vlrCalcAjustado, BigDecimal pctReajusteAnual) {
        public static ProgramaResponse de(ProgramaJpaEntity e) {
            return new ProgramaResponse(e.getCodigo(), e.getNome(), e.getSigla(), e.getStatus(),
                    e.getTipo(), e.getValorBase(), e.getFatorK(),
                    e.getVlrCalcAjustado(), e.getPctReajusteAnual());
        }
    }
    public record FaixaResponse(int ordem, BigDecimal rendaAte, BigDecimal fator) {}
    public record ParamRegionalResponse(int regiao, BigDecimal fatorExtra) {}
}
