package br.gov.sifap.pagamentos.application;

import br.gov.sifap.pagamentos.domain.BeneficiarioSnapshot;
import java.math.BigDecimal;
import java.util.List;

/**
 * Porta de saída para obter os beneficiários elegíveis para um ciclo.
 *
 * <p>O adapter padrão (in-memory) atende o protótipo do Estágio 3. Em produção
 * será substituído por uma chamada ao módulo {@code beneficiarios} (ADR-0002).</p>
 */
public interface BeneficiariosPort {

    /** Retorna apenas beneficiários com status ATIVO (REQ-PAY-010), ordenados por CPF (REQ-PAY-011). */
    List<BeneficiarioSnapshot> listarAtivosOrdenadosPorCpf();

    /** Valor base do programa para a competência (proxy para programas::api no MVP). */
    BigDecimal valorBaseDoProgramaAtivo(String programaCodigo);
}
