package br.gov.sifap.beneficiarios.application;

import br.gov.sifap.beneficiarios.domain.*;
import br.gov.sifap.beneficiarios.infrastructure.persistence.*;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Substitui ATUFCAD/CADBENEF na branch de alteração — espelha as validações
 * de {@code CADBENEF.NSN} (linhas 280-390): só atualiza campos fornecidos
 * (semântica de PATCH), revalida CPF/UF/idade, reaplica regra dos 75 anos.
 */
@Service
public class AlterarBeneficiarioUseCase {

    private final BeneficiarioRepository repo;
    private final Clock clock;

    public AlterarBeneficiarioUseCase(BeneficiarioRepository repo, Clock clock) {
        this.repo = repo; this.clock = clock;
    }

    @Transactional
    public BeneficiarioJpaEntity executar(UUID id, AlteracaoBeneficiarioCommand cmd) {
        var e = repo.findById(id)
                .orElseThrow(() -> new BeneficiarioNaoEncontradoException(id.toString()));

        if (cmd.nome() != null) { ValidadorBeneficiario.validarNome(cmd.nome()); e.setNome(cmd.nome()); }
        if (cmd.uf() != null)    { ValidadorBeneficiario.validarUf(cmd.uf()); e.setUf(cmd.uf()); }
        if (cmd.dtNascimento() != null) {
            ValidadorBeneficiario.validarDataNascimento(cmd.dtNascimento());
            e.setDtNascimento(cmd.dtNascimento());
            // reaplica regra 75
            StatusBeneficiario nv = ValidadorBeneficiario.aplicarRegraIdade(
                    StatusBeneficiario.valueOf(e.getStatus()), cmd.dtNascimento());
            e.setStatus(nv.name());
        }
        if (cmd.rendaMensal() != null) e.setRendaMensal(cmd.rendaMensal());
        if (cmd.regiao() != null)      e.setRegiao(cmd.regiao());
        if (cmd.email() != null)       e.setEmail(cmd.email());
        if (cmd.telCelular() != null)  e.setTelCelular(cmd.telCelular());
        if (cmd.status() != null)      e.setStatus(cmd.status().name());
        if (cmd.motSituacao() != null) e.setMotSituacao(cmd.motSituacao());

        e.setAtualizadoEm(OffsetDateTime.now(clock));
        return e;
    }
}
