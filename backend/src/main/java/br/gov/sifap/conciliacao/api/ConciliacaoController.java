package br.gov.sifap.conciliacao.api;

import br.gov.sifap.conciliacao.application.ConciliarRemessaUseCase;
import br.gov.sifap.conciliacao.infrastructure.persistence.RegistroConciliacaoRepository;
import jakarta.validation.constraints.Pattern;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** API REST do bounded context conciliacao. Espelha BATCHCON. */
@RestController
@RequestMapping("/api/v1/conciliacao")
public class ConciliacaoController {

    private final ConciliarRemessaUseCase useCase;
    private final RegistroConciliacaoRepository registroRepo;

    public ConciliacaoController(ConciliarRemessaUseCase u, RegistroConciliacaoRepository r) {
        this.useCase = u; this.registroRepo = r;
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ConciliarRemessaUseCase.ResultadoConciliacao upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam @Pattern(regexp = "\\d{6}") String competencia) throws IOException {
        return useCase.executar(file.getOriginalFilename(), competencia, file.getInputStream());
    }

    @GetMapping("/{arquivoId}/registros")
    public List<RegistroResponse> registros(@PathVariable UUID arquivoId) {
        return registroRepo.findByArquivoId(arquivoId).stream()
                .map(r -> new RegistroResponse(r.getCpf(), r.getNumPagto(), r.getVlrRetorno(),
                        r.getDtPagamento() == null ? null : r.getDtPagamento().toString(),
                        r.getCodRetorno(), r.getResultado(), r.getDiferenca()))
                .toList();
    }

    public record RegistroResponse(String cpf, Long numPagto, java.math.BigDecimal valorRetorno,
                                   String dtPagamento, String codRetorno, String resultado,
                                   java.math.BigDecimal diferenca) {}
}
