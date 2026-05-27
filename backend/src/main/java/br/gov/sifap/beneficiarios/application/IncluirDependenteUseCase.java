package br.gov.sifap.beneficiarios.application;

import br.gov.sifap.beneficiarios.domain.*;
import br.gov.sifap.beneficiarios.infrastructure.persistence.*;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Substitui {@code CADDEPEND.NSN}.
 *
 * <p>Regras:</p>
 * <ul>
 *   <li>Bloqueia se titular está CANCELADO ou DESLIGADO (linhas 88-97);</li>
 *   <li>Limite de 5 dependentes ATIVOS por titular (PE máx 10 no DDM,
 *       mas regra de negócio limita a 5 - linha 121);</li>
 *   <li>CPF de dependente único por titular (linhas 110-118).</li>
 * </ul>
 */
@Service
public class IncluirDependenteUseCase {

    private static final int LIMITE_DEPENDENTES_ATIVOS = 5;

    private final BeneficiarioRepository benefRepo;
    private final DependenteRepository depRepo;
    private final Clock clock;

    public IncluirDependenteUseCase(BeneficiarioRepository benefRepo,
                                    DependenteRepository depRepo, Clock clock) {
        this.benefRepo = benefRepo;
        this.depRepo = depRepo;
        this.clock = clock;
    }

    @Transactional
    public DependenteJpaEntity executar(UUID beneficiarioId, NovoDependenteCommand cmd) {
        var titular = benefRepo.findById(beneficiarioId)
                .orElseThrow(() -> new BeneficiarioNaoEncontradoException(beneficiarioId.toString()));

        StatusBeneficiario st = StatusBeneficiario.valueOf(titular.getStatus());
        if (st == StatusBeneficiario.CANCELADO || st == StatusBeneficiario.DESLIGADO) {
            throw new TitularInativoException(st.name());
        }

        if (depRepo.countByBeneficiarioIdAndSituacao(beneficiarioId, "ATIVO") >= LIMITE_DEPENDENTES_ATIVOS) {
            throw new LimiteDependentesAtingidoException();
        }

        if (cmd.cpf() != null && depRepo.existsByBeneficiarioIdAndCpf(beneficiarioId, cmd.cpf())) {
            throw new IllegalArgumentException("dependente.cpf-duplicado");
        }

        int idade = cmd.dtNascimento() == null
                ? cmd.idade()
                : Period.between(cmd.dtNascimento(), LocalDate.now(clock)).getYears();

        var dep = new DependenteJpaEntity(
                UUID.randomUUID(), beneficiarioId, cmd.nome(), idade,
                cmd.cpf(), cmd.dtNascimento(),
                cmd.parentesco() == null ? null : cmd.parentesco().name(),
                cmd.documento(),
                cmd.sexo() == null ? null : cmd.sexo().name(),
                cmd.indDeficiencia());
        return depRepo.save(dep);
    }
}
