package br.gov.sifap.beneficiarios.api;

import br.gov.sifap.beneficiarios.application.*;
import br.gov.sifap.beneficiarios.domain.*;
import br.gov.sifap.beneficiarios.infrastructure.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * API REST do bounded context beneficiarios.
 * Cobre CADBENEF (POST/PUT), CADDEPEND (POST /dependentes), CONSBENF (GET),
 * VALELEG (GET /elegibilidade).
 */
@RestController
@RequestMapping("/api/v1/beneficiarios")
public class BeneficiarioController {

    private final CadastrarBeneficiarioUseCase cadastrar;
    private final AlterarBeneficiarioUseCase alterar;
    private final IncluirDependenteUseCase incluirDep;
    private final ConsultarBeneficiarioUseCase consultar;
    private final AvaliarElegibilidadeUseCase elegibilidade;

    public BeneficiarioController(CadastrarBeneficiarioUseCase c,
                                  AlterarBeneficiarioUseCase a,
                                  IncluirDependenteUseCase i,
                                  ConsultarBeneficiarioUseCase s,
                                  AvaliarElegibilidadeUseCase e) {
        this.cadastrar = c; this.alterar = a; this.incluirDep = i;
        this.consultar = s; this.elegibilidade = e;
    }

    @PostMapping
    public ResponseEntity<BeneficiarioResponse> cadastrar(@Valid @RequestBody BeneficiarioRequest req) {
        var cmd = new NovoBeneficiarioCommand(
                req.cpf(), req.nome(), req.regiao(),
                req.rendaMensal(), req.idade(), req.programaCodigo(),
                req.nomeMae(), req.nomePai(), req.dtNascimento(),
                req.sexo() == null ? null : Sexo.valueOf(req.sexo()),
                req.estCivil() == null ? null : EstadoCivil.valueOf(req.estCivil()),
                req.rgNumero(), req.rgOrgao(), req.rgUf(),
                req.endereco() == null ? null : new Endereco(
                        req.endereco().logradouro(), req.endereco().numero(),
                        req.endereco().complemento(), req.endereco().bairro(),
                        req.endereco().municipio(), req.endereco().uf(), req.endereco().cep()),
                req.telFixo(), req.telCelular(), req.email(), req.nis(),
                req.dtInicioBenef(), req.biometria(), req.codElegibilidade());
        var saved = cadastrar.executar(cmd);
        return ResponseEntity.created(URI.create("/api/v1/beneficiarios/" + saved.getId()))
                .body(BeneficiarioResponse.de(saved));
    }

    @PutMapping("/{id}")
    public BeneficiarioResponse alterar(@PathVariable UUID id,
                                        @RequestBody AlteracaoRequest req) {
        var cmd = new AlteracaoBeneficiarioCommand(
                req.nome(), req.regiao(), req.rendaMensal(), req.dtNascimento(),
                req.email(), req.telCelular(), req.uf(),
                req.status() == null ? null : StatusBeneficiario.valueOf(req.status()),
                req.motSituacao());
        return BeneficiarioResponse.de(alterar.executar(id, cmd));
    }

    @GetMapping("/{id}")
    public ConsultaResponse porId(@PathVariable UUID id) {
        return ConsultaResponse.de(consultar.porId(id));
    }

    @GetMapping(params = "cpf")
    public ConsultaResponse porCpf(@RequestParam String cpf) {
        return ConsultaResponse.de(consultar.porCpf(cpf));
    }

    @GetMapping(params = "nis")
    public ConsultaResponse porNis(@RequestParam String nis) {
        return ConsultaResponse.de(consultar.porNis(nis));
    }

    @PostMapping("/{id}/dependentes")
    public ResponseEntity<DependenteResponse> incluirDependente(@PathVariable UUID id,
                                                                 @Valid @RequestBody DependenteRequest req) {
        var cmd = new NovoDependenteCommand(
                req.nome(), req.idade(), req.cpf(), req.dtNascimento(),
                req.parentesco() == null ? null : Parentesco.valueOf(req.parentesco()),
                req.documento(),
                req.sexo() == null ? null : Sexo.valueOf(req.sexo()),
                req.indDeficiencia());
        var saved = incluirDep.executar(id, cmd);
        return ResponseEntity.created(URI.create("/api/v1/beneficiarios/" + id + "/dependentes/" + saved.getId()))
                .body(DependenteResponse.de(saved));
    }

    @GetMapping("/{id}/elegibilidade")
    public AvaliarElegibilidadeUseCase.ResultadoElegibilidade elegibilidade(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "A") String tipoPrograma) {
        return elegibilidade.avaliar(id, tipoPrograma.charAt(0));
    }

    // ---- DTOs ----
    public record BeneficiarioRequest(
            @NotBlank @Pattern(regexp = "\\d{11}") String cpf,
            @NotBlank String nome,
            @Min(1) @Max(99) int regiao,
            @NotNull @DecimalMin("0.00") BigDecimal rendaMensal,
            @Min(0) int idade,
            @NotBlank String programaCodigo,
            String nomeMae, String nomePai,
            LocalDate dtNascimento, String sexo, String estCivil,
            String rgNumero, String rgOrgao, String rgUf,
            EnderecoDto endereco,
            String telFixo, String telCelular, String email, String nis,
            LocalDate dtInicioBenef, String biometria, String codElegibilidade) {}

    public record EnderecoDto(String logradouro, String numero, String complemento,
                              String bairro, String municipio, String uf, String cep) {}

    public record AlteracaoRequest(String nome, Integer regiao, BigDecimal rendaMensal,
                                   LocalDate dtNascimento, String email, String telCelular,
                                   String uf, String status, String motSituacao) {}

    public record DependenteRequest(
            @NotBlank String nome, @Min(0) int idade,
            @Pattern(regexp = "\\d{11}") String cpf,
            LocalDate dtNascimento, String parentesco,
            String documento, String sexo, String indDeficiencia) {}

    public record BeneficiarioResponse(UUID id, Long numInscricao, String cpf, String nome,
                                       int regiao, BigDecimal rendaMensal, int idade,
                                       String status, String programaCodigo,
                                       String documentosOk, String codElegibilidade) {
        public static BeneficiarioResponse de(BeneficiarioJpaEntity e) {
            return new BeneficiarioResponse(e.getId(), e.getNumInscricao(), e.getCpf(),
                    e.getNome(), e.getRegiao(), e.getRendaMensal(), e.getIdade(),
                    e.getStatus(), e.getProgramaCodigo(), e.getDocumentosOk(), e.getCodElegibilidade());
        }
    }

    public record DependenteResponse(UUID id, UUID beneficiarioId, String nome,
                                     String cpf, String parentesco, String situacao) {
        public static DependenteResponse de(DependenteJpaEntity d) {
            return new DependenteResponse(d.getId(), d.getBeneficiarioId(), d.getNome(),
                    d.getCpf(), d.getParentesco(), d.getSituacao());
        }
    }

    public record ConsultaResponse(BeneficiarioResponse titular, String cpfMascarado,
                                   List<DependenteResponse> dependentes) {
        public static ConsultaResponse de(ConsultarBeneficiarioUseCase.ResultadoConsulta r) {
            return new ConsultaResponse(
                    BeneficiarioResponse.de(r.titular()),
                    r.cpfMascarado(),
                    r.dependentes().stream().map(DependenteResponse::de).toList());
        }
    }
}
