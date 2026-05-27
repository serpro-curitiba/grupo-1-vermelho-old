package br.gov.sifap.beneficiarios.application;

import br.gov.sifap.beneficiarios.domain.StatusBeneficiario;
import br.gov.sifap.beneficiarios.infrastructure.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Substitui {@code VALELEG.NSN}. Avalia se um beneficiário é elegível para
 * um tipo de programa (A/P/T) considerando:
 * <ul>
 *   <li>status ATIVO obrigatório;</li>
 *   <li>região 99 = inelegibilidade SIFAP (uso internacional/diplomático);
 *       no legado retorna elegível-auto — mantemos paridade;</li>
 *   <li>Programa A (Assistencial): renda ≤ 600 OU presença de dependentes;</li>
 *   <li>Programa P (Previdenciário): idade ≥ 60;</li>
 *   <li>Programa T (Trabalho): idade 16-65;</li>
 *   <li>Código de elegibilidade: letra 1 'R' exige NIS preenchido;
 *       letra 2 'D' exige ao menos 1 dependente ativo.</li>
 * </ul>
 */
@Service
public class AvaliarElegibilidadeUseCase {

    private final BeneficiarioRepository benefRepo;
    private final DependenteRepository depRepo;

    public AvaliarElegibilidadeUseCase(BeneficiarioRepository b, DependenteRepository d) {
        this.benefRepo = b; this.depRepo = d;
    }

    @Transactional(readOnly = true)
    public ResultadoElegibilidade avaliar(UUID beneficiarioId, char tipoPrograma) {
        var benef = benefRepo.findById(beneficiarioId)
                .orElseThrow(() -> new BeneficiarioNaoEncontradoException(beneficiarioId.toString()));

        if (!StatusBeneficiario.ATIVO.name().equals(benef.getStatus())) {
            return ResultadoElegibilidade.no("status nao ATIVO");
        }
        if (benef.getRegiao() != null && benef.getRegiao() == 99) {
            return ResultadoElegibilidade.yes("regiao 99 - elegibilidade automatica");
        }

        long qtdDeps = depRepo.countByBeneficiarioIdAndSituacao(beneficiarioId, "ATIVO");
        BigDecimal renda = benef.getRendaMensal();
        int idade = benef.getIdade() == null ? 0 : benef.getIdade();

        String motivo = null;
        switch (Character.toUpperCase(tipoPrograma)) {
            case 'A' -> {
                boolean rendaOk = renda != null && renda.compareTo(new BigDecimal("600.00")) <= 0;
                if (!rendaOk && qtdDeps == 0) {
                    motivo = "tipo A - renda > 600 sem dependentes";
                }
            }
            case 'P' -> { if (idade < 60) motivo = "tipo P - idade < 60"; }
            case 'T' -> { if (idade < 16 || idade > 65) motivo = "tipo T - idade fora 16..65"; }
            default -> { motivo = "tipo programa desconhecido: " + tipoPrograma; }
        }
        if (motivo != null) return ResultadoElegibilidade.no(motivo);

        String cod = benef.getCodElegibilidade();
        if (cod != null && cod.length() >= 1 && cod.charAt(0) == 'R'
                && (benef.getNis() == null || benef.getNis().isBlank())) {
            return ResultadoElegibilidade.no("cod elegibilidade R exige NIS");
        }
        if (cod != null && cod.length() >= 2 && cod.charAt(1) == 'D' && qtdDeps == 0) {
            return ResultadoElegibilidade.no("cod elegibilidade D exige dependentes");
        }

        return ResultadoElegibilidade.yes("ok");
    }

    public record ResultadoElegibilidade(boolean elegivel, String motivo) {
        public static ResultadoElegibilidade yes(String m) { return new ResultadoElegibilidade(true, m); }
        public static ResultadoElegibilidade no(String m)  { return new ResultadoElegibilidade(false, m); }
    }
}
