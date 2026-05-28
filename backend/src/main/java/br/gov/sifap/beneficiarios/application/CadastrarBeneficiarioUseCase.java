package br.gov.sifap.beneficiarios.application;

import br.gov.sifap.beneficiarios.domain.*;
import br.gov.sifap.beneficiarios.infrastructure.persistence.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Period;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Substitui {@code CADBENEF.NSN} (linhas 50-260).
 *
 * <p>Inclui: validação de CPF (VALBENEF/VALIDA-CPF-COMPLETO), validação de
 * documentos (VALDOCS), regra de idade &gt; 75 → SUSPENSO, geração de número
 * de inscrição via sequence Postgres, e timestamps de cadastro.</p>
 */
@Service
public class CadastrarBeneficiarioUseCase {

    private final BeneficiarioRepository repo;
    private final Clock clock;

    @PersistenceContext
    private EntityManager em;

    public CadastrarBeneficiarioUseCase(BeneficiarioRepository repo, Clock clock) {
        this.repo = repo;
        this.clock = clock;
    }

    @Transactional
    public BeneficiarioJpaEntity executar(NovoBeneficiarioCommand cmd) {
        // VALBENEF — CPF + nome + UF + data
        Cpf cpfVo = Cpf.de(cmd.cpf());
        ValidadorBeneficiario.validarNome(cmd.nome());
        ValidadorBeneficiario.validarUf(cmd.rgUf());
        if (cmd.endereco() != null) {
            ValidadorBeneficiario.validarUf(cmd.endereco().uf());
        }
        ValidadorBeneficiario.validarDataNascimento(cmd.dtNascimento());

        // CADBENEF — não permite CPF duplicado
        if (repo.findByCpf(cpfVo.numero()).isPresent()) {
            throw new CpfJaCadastradoException(cpfVo.numero());
        }

        // VALDOCS — documentos OK (S/N)
        boolean docsOk = ValidadorDocumentos.documentosOk(cpfVo.numero(), cmd.rgNumero());

        // regra idade > 75 → SUSPENSO
        StatusBeneficiario status = ValidadorBeneficiario.aplicarRegraIdade(
                StatusBeneficiario.ATIVO, cmd.dtNascimento());

        int idade = cmd.dtNascimento() == null
                ? cmd.idade()
                : Period.between(cmd.dtNascimento(), LocalDate.now(clock)).getYears();

        OffsetDateTime agora = OffsetDateTime.now(clock);
        var e = new BeneficiarioJpaEntity(
                UUID.randomUUID(), cpfVo.numero(), cmd.nome(), cmd.regiao(),
                cmd.rendaMensal() == null ? BigDecimal.ZERO : cmd.rendaMensal(),
                idade, status.name(), cmd.programaCodigo(), agora);

        // próximo num_inscricao via sequence
        Number prox = (Number) em.createNativeQuery("SELECT nextval('beneficiarios.seq_num_inscricao')").getSingleResult();
        e.setNumInscricao(prox.longValue());

        e.setNomeMae(cmd.nomeMae()); e.setNomePai(cmd.nomePai());
        e.setDtNascimento(cmd.dtNascimento());
        if (cmd.sexo() != null) e.setSexo(cmd.sexo().name());
        if (cmd.estCivil() != null) e.setEstCivil(cmd.estCivil().codigoLegado());
        e.setRgNumero(cmd.rgNumero()); e.setRgOrgao(cmd.rgOrgao()); e.setRgUf(cmd.rgUf());
        if (cmd.endereco() != null) {
            var en = cmd.endereco();
            e.setLogradouro(en.logradouro()); e.setNumero(en.numero());
            e.setComplemento(en.complemento()); e.setBairro(en.bairro());
            e.setMunicipio(en.municipio()); e.setUf(en.uf()); e.setCep(en.cep());
        }
        e.setTelFixo(cmd.telFixo()); e.setTelCelular(cmd.telCelular());
        e.setEmail(cmd.email()); e.setNis(cmd.nis());
        e.setDtCadastro(LocalDate.now(clock));
        e.setDtInicioBenef(cmd.dtInicioBenef());
        e.setDocumentosOk(docsOk ? "S" : "N");
        e.setIndBiometria(cmd.biometria() == null ? "N" : cmd.biometria());
        e.setCodElegibilidade(cmd.codElegibilidade());

        return repo.save(e);
    }
}
