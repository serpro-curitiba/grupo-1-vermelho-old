package br.gov.sifap.beneficiarios.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Cadastro de desconto recorrente do beneficiário — usado por CALCDSCT. */
@Entity
@Table(name = "desconto_cadastro", schema = "beneficiarios")
public class DescontoCadastroJpaEntity {

    @Id @Column(nullable = false, updatable = false) private UUID id;
    @Column(name = "beneficiario_id", nullable = false) private UUID beneficiarioId;
    @Column(nullable = false, length = 1) private String tipo;          // C/I/J/S/P/A
    @Column(name = "valor_fixo", precision = 9, scale = 2) private BigDecimal valorFixo;
    @Column(precision = 5, scale = 2) private BigDecimal percentual;
    @Column(name = "dt_inicio", nullable = false) private LocalDate dtInicio;
    @Column(name = "dt_fim") private LocalDate dtFim;
    @Column(name = "num_processo", length = 20) private String numProcesso;
    @Column(name = "criado_em", nullable = false) private OffsetDateTime criadoEm;

    protected DescontoCadastroJpaEntity() {}

    public DescontoCadastroJpaEntity(UUID id, UUID beneficiarioId, String tipo,
                                     BigDecimal valorFixo, BigDecimal percentual,
                                     LocalDate dtInicio, LocalDate dtFim,
                                     String numProcesso, OffsetDateTime criadoEm) {
        this.id = id; this.beneficiarioId = beneficiarioId; this.tipo = tipo;
        this.valorFixo = valorFixo; this.percentual = percentual;
        this.dtInicio = dtInicio; this.dtFim = dtFim;
        this.numProcesso = numProcesso; this.criadoEm = criadoEm;
    }

    public UUID getId() { return id; }
    public UUID getBeneficiarioId() { return beneficiarioId; }
    public String getTipo() { return tipo; }
    public BigDecimal getValorFixo() { return valorFixo; }
    public BigDecimal getPercentual() { return percentual; }
    public LocalDate getDtInicio() { return dtInicio; }
    public LocalDate getDtFim() { return dtFim; }
    public String getNumProcesso() { return numProcesso; }
    public OffsetDateTime getCriadoEm() { return criadoEm; }
}
