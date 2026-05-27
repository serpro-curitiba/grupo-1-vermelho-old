package br.gov.sifap.pagamentos.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Snapshot imutável do beneficiário no momento da geração do ciclo.
 * Usado pelo cálculo para garantir reprodutibilidade e auditoria.
 *
 * <p>Os campos espelham {@code BENEFICIARIO-V} (origem Adabas), com o Periodic Group
 * {@code DEPENDENTES} desnormalizado em {@code qtdDependentes}.</p>
 */
public record BeneficiarioSnapshot(
        UUID beneficiarioId,
        String cpf,
        String nome,
        int regiao,
        int qtdDependentes,
        BigDecimal rendaMensal,
        int idade,
        String statusOrigem,
        String programaCodigo,
        OffsetDateTime fotografadoEm) {
}
