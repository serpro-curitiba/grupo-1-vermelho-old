package br.gov.sifap.programas.application;

import br.gov.sifap.programas.domain.FatorK;
import br.gov.sifap.programas.infrastructure.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Substitui {@code CADPROG.NSN} (linhas 50-200). Cadastra um programa social com
 * suas faixas de cálculo (PE máx 5 do DDM) e parâmetros regionais (PE máx 6).
 * Calcula o FATOR-K e o valor calculado ajustado.
 */
@Service
public class CadastrarProgramaUseCase {

    private final ProgramaRepository progRepo;
    private final FaixaCalculoRepository faixaRepo;
    private final ParametroRegionalRepository paramRepo;

    public CadastrarProgramaUseCase(ProgramaRepository p, FaixaCalculoRepository f,
                                    ParametroRegionalRepository pr) {
        this.progRepo = p; this.faixaRepo = f; this.paramRepo = pr;
    }

    @Transactional
    public ProgramaJpaEntity executar(NovoProgramaCommand cmd) {
        if (progRepo.existsById(cmd.codigo())) {
            throw new IllegalArgumentException("programa.codigo-duplicado: " + cmd.codigo());
        }
        var e = new ProgramaJpaEntity(cmd.codigo(), cmd.nome(), "ATIVO", cmd.valorBase(), cmd.tipo());
        e.setSigla(cmd.sigla()); e.setCodElegibilidade(cmd.codElegibilidade());
        e.setDtInicio(cmd.dtInicio()); e.setDtFim(cmd.dtFim());
        e.setRendaMax(cmd.rendaMax()); e.setIdadeMin(cmd.idadeMin()); e.setIdadeMax(cmd.idadeMax());
        e.setPctReajusteAnual(cmd.pctReajusteAnual() == null ? BigDecimal.ZERO : cmd.pctReajusteAnual());
        e.setFatorK(FatorK.calcular(e.getPctReajusteAnual()));
        e.setVlrCalcAjustado(FatorK.aplicar(cmd.valorBase(), e.getPctReajusteAnual()));
        progRepo.save(e);

        if (cmd.faixas() != null) {
            if (cmd.faixas().size() > 5) throw new IllegalArgumentException("faixas.maximo-5");
            for (var f : cmd.faixas()) {
                faixaRepo.save(new FaixaCalculoJpaEntity(
                        UUID.randomUUID(), e.getCodigo(), f.ordem(), f.rendaAte(), f.fator()));
            }
        }
        if (cmd.parametrosRegionais() != null) {
            for (var p : cmd.parametrosRegionais()) {
                paramRepo.save(new ParametroRegionalJpaEntity(
                        UUID.randomUUID(), e.getCodigo(), p.regiao(), p.fatorExtra()));
            }
        }
        return e;
    }

    public record NovoProgramaCommand(
            String codigo, String nome, String sigla, BigDecimal valorBase,
            String tipo, String codElegibilidade,
            LocalDate dtInicio, LocalDate dtFim,
            BigDecimal rendaMax, Integer idadeMin, Integer idadeMax,
            BigDecimal pctReajusteAnual,
            List<FaixaInput> faixas, List<ParamRegionalInput> parametrosRegionais) {}

    public record FaixaInput(int ordem, BigDecimal rendaAte, BigDecimal fator) {}
    public record ParamRegionalInput(int regiao, BigDecimal fatorExtra) {}
}
