package br.gov.sifap.programas.application;

import br.gov.sifap.programas.infrastructure.persistence.IndiceIpcaJpaEntity;
import br.gov.sifap.programas.infrastructure.persistence.IndiceIpcaRepository;
import br.gov.sifap.shared.TruncamentoMonetario;
import java.math.BigDecimal;
import java.time.YearMonth;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Substitui {@code CALCCORR.NSN}. Aplica correção monetária IPCA mensal
 * entre dois períodos a um valor original.
 *
 * <p><b>Nota arqueológica:</b> bloco "Plano Verão 1989-1991" do legado
 * (linhas 220-260, comentado com {@code *} desde 2003) NÃO é portado.</p>
 */
@Service
public class CalculadoraCorrecaoService {

    private final IndiceIpcaRepository ipcaRepo;

    public CalculadoraCorrecaoService(IndiceIpcaRepository ipcaRepo) {
        this.ipcaRepo = ipcaRepo;
    }

    @Transactional(readOnly = true)
    public BigDecimal corrigir(BigDecimal valorOriginal, YearMonth de, YearMonth ate) {
        if (valorOriginal == null) return BigDecimal.ZERO;
        if (de == null || ate == null || de.isAfter(ate)) return TruncamentoMonetario.truncar(valorOriginal);

        var serie = ipcaRepo.intervalo(de.getYear(), de.getMonthValue(), ate.getYear(), ate.getMonthValue());
        BigDecimal acumulado = BigDecimal.ONE;
        for (IndiceIpcaJpaEntity i : serie) {
            acumulado = acumulado.multiply(BigDecimal.ONE.add(i.getFator()));
        }
        return TruncamentoMonetario.truncar(valorOriginal.multiply(acumulado));
    }
}
