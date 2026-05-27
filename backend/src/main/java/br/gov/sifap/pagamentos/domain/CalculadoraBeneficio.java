package br.gov.sifap.pagamentos.domain;

import br.gov.sifap.shared.TruncamentoMonetario;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Reescrita fiel de {@code CALCBENF.NSN} (linhas 80-380) + cálculo embarcado em
 * {@code BATCHPGT.NSN} (linhas 200-360).
 *
 * <ul>
 *   <li><b>Fator regional</b>: tabela #TAB-REG[1..27] real do legado;</li>
 *   <li><b>Fator de renda</b>: 5 faixas 300/600/1000/1500/9999 →
 *       1.0/0.85/0.70/0.55/0.40;</li>
 *   <li><b>Fator familiar progressivo</b>: 0=1.0; 1-2=1.0+0.05·n;
 *       3-4=1.10+0.03·(n-2); ≥5=1.16+0.02·(n-4);</li>
 *   <li><b>Fator idade</b>: &lt;18=1.05; 60-64=1.10; ≥65=1.15;</li>
 *   <li><b>Truncamento</b> (não arredondamento) — paridade Natural;</li>
 *   <li><b>13º</b> em dezembro: valorBase × fatorRegional × fatorIdade;</li>
 *   <li><b>Abono</b> 15% sobre valorBase para programas tipo "A" em dezembro.</li>
 * </ul>
 */
public final class CalculadoraBeneficio {

    private final Map<Integer, BigDecimal> tabelaRegional;
    private final BigDecimal fatorReajusteVigente;

    public CalculadoraBeneficio(Map<Integer, BigDecimal> tabelaRegional,
                                BigDecimal fatorReajusteVigente) {
        this.tabelaRegional = Map.copyOf(tabelaRegional);
        this.fatorReajusteVigente = fatorReajusteVigente == null ? BigDecimal.ONE : fatorReajusteVigente;
    }

    public ComposicaoValor calcular(BigDecimal valorBase, BeneficiarioSnapshot snap) {
        return new ComposicaoValor(
                valorBase,
                fatorRegional(snap.regiao()),
                fatorFamiliar(snap.qtdDependentes()),
                fatorRenda(snap.rendaMensal()),
                fatorIdade(snap.idade()),
                fatorReajusteVigente);
    }

    /** 13º (mês 12) — fórmula reduzida do CALCBENF linhas 290-310. */
    public BigDecimal decimoTerceiro(BigDecimal valorBase, BeneficiarioSnapshot snap) {
        BigDecimal v = valorBase
                .multiply(fatorRegional(snap.regiao()))
                .multiply(fatorIdade(snap.idade()));
        return TruncamentoMonetario.truncar(v);
    }

    /** Abono 15% — somente programas tipo "A" em dezembro (CALCBENF linhas 320-340). */
    public BigDecimal abono(BigDecimal valorBase, String tipoPrograma, int mes) {
        if (mes != 12 || !"A".equalsIgnoreCase(tipoPrograma)) return BigDecimal.ZERO;
        return TruncamentoMonetario.truncar(valorBase.multiply(new BigDecimal("0.15")));
    }

    BigDecimal fatorRegional(int regiao) {
        if (regiao < 1 || regiao > 27) return BigDecimal.ONE;
        return tabelaRegional.getOrDefault(regiao, BigDecimal.ONE);
    }

    BigDecimal fatorRenda(BigDecimal renda) {
        if (renda == null) return BigDecimal.ONE;
        if (renda.compareTo(new BigDecimal("300"))  <= 0) return new BigDecimal("1.0000");
        if (renda.compareTo(new BigDecimal("600"))  <= 0) return new BigDecimal("0.8500");
        if (renda.compareTo(new BigDecimal("1000")) <= 0) return new BigDecimal("0.7000");
        if (renda.compareTo(new BigDecimal("1500")) <= 0) return new BigDecimal("0.5500");
        return new BigDecimal("0.4000");
    }

    BigDecimal fatorFamiliar(int n) {
        if (n <= 0) return BigDecimal.ONE.setScale(4);
        BigDecimal f;
        if (n <= 2) {
            f = BigDecimal.ONE.add(new BigDecimal("0.05").multiply(BigDecimal.valueOf(n)));
        } else if (n <= 4) {
            f = new BigDecimal("1.10").add(new BigDecimal("0.03").multiply(BigDecimal.valueOf(n - 2)));
        } else {
            f = new BigDecimal("1.16").add(new BigDecimal("0.02").multiply(BigDecimal.valueOf(n - 4)));
        }
        return f.setScale(4, RoundingMode.HALF_UP);
    }

    BigDecimal fatorIdade(int idade) {
        if (idade < 18)  return new BigDecimal("1.0500");
        if (idade >= 65) return new BigDecimal("1.1500");
        if (idade >= 60) return new BigDecimal("1.1000");
        return BigDecimal.ONE.setScale(4);
    }
}
