package br.gov.sifap.beneficiarios.application;

import br.gov.sifap.beneficiarios.infrastructure.persistence.*;
import br.gov.sifap.shared.CpfMask;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Substitui {@code CONSBENF.NSN}. Suporta busca por CPF ou NIS e devolve
 * o titular + lista de dependentes + máscara de CPF.
 */
@Service
public class ConsultarBeneficiarioUseCase {

    private final BeneficiarioRepository benefRepo;
    private final DependenteRepository depRepo;

    public ConsultarBeneficiarioUseCase(BeneficiarioRepository b, DependenteRepository d) {
        this.benefRepo = b; this.depRepo = d;
    }

    @Transactional(readOnly = true)
    public ResultadoConsulta porCpf(String cpf) {
        var benef = benefRepo.findByCpf(cpf)
                .orElseThrow(() -> new BeneficiarioNaoEncontradoException("cpf=" + CpfMask.mask(cpf)));
        return montar(benef);
    }

    @Transactional(readOnly = true)
    public ResultadoConsulta porNis(String nis) {
        var benef = benefRepo.findByNis(nis)
                .orElseThrow(() -> new BeneficiarioNaoEncontradoException("nis=" + nis));
        return montar(benef);
    }

    @Transactional(readOnly = true)
    public ResultadoConsulta porId(UUID id) {
        var benef = benefRepo.findById(id)
                .orElseThrow(() -> new BeneficiarioNaoEncontradoException(id.toString()));
        return montar(benef);
    }

    private ResultadoConsulta montar(BeneficiarioJpaEntity benef) {
        List<DependenteJpaEntity> deps = depRepo.findByBeneficiarioIdOrderByNome(benef.getId());
        return new ResultadoConsulta(benef, deps, CpfMask.mask(benef.getCpf()));
    }

    public record ResultadoConsulta(BeneficiarioJpaEntity titular,
                                    List<DependenteJpaEntity> dependentes,
                                    String cpfMascarado) {}
}
