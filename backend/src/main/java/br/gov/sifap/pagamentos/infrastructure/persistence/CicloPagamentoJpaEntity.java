package br.gov.sifap.pagamentos.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "ciclo_pagamento", schema = "pagamentos")
public class CicloPagamentoJpaEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 6)
    private String competencia;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "total_candidatos", nullable = false)
    private int totalCandidatos;

    @Column(name = "total_pagamentos", nullable = false)
    private int totalPagamentos;

    @Column(name = "total_ignorados", nullable = false)
    private int totalIgnorados;

    @Column(name = "valor_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Column(name = "iniciado_em", nullable = false)
    private OffsetDateTime iniciadoEm;

    @Column(name = "calculado_em")
    private OffsetDateTime calculadoEm;

    @Column(name = "requisitante_id", nullable = false, length = 36)
    private String requisitanteId;

    @Column(name = "total_bruto",    nullable = false, precision = 15, scale = 2)
    private BigDecimal totalBruto    = BigDecimal.ZERO;
    @Column(name = "total_desconto", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalDesconto = BigDecimal.ZERO;
    @Column(name = "total_liquido",  nullable = false, precision = 15, scale = 2)
    private BigDecimal totalLiquido  = BigDecimal.ZERO;
    @Column(name = "total_abono",    nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAbono    = BigDecimal.ZERO;
    @Column(name = "total_13",       nullable = false, precision = 15, scale = 2)
    private BigDecimal total13       = BigDecimal.ZERO;

    protected CicloPagamentoJpaEntity() {}

    public CicloPagamentoJpaEntity(UUID id, String competencia, String status,
                                   OffsetDateTime iniciadoEm, String requisitanteId) {
        this.id = id;
        this.competencia = competencia;
        this.status = status;
        this.iniciadoEm = iniciadoEm;
        this.requisitanteId = requisitanteId;
    }

    public UUID getId() { return id; }
    public String getCompetencia() { return competencia; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getTotalCandidatos() { return totalCandidatos; }
    public void setTotalCandidatos(int v) { this.totalCandidatos = v; }
    public int getTotalPagamentos() { return totalPagamentos; }
    public void setTotalPagamentos(int v) { this.totalPagamentos = v; }
    public int getTotalIgnorados() { return totalIgnorados; }
    public void setTotalIgnorados(int v) { this.totalIgnorados = v; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal v) { this.valorTotal = v; }
    public OffsetDateTime getIniciadoEm() { return iniciadoEm; }
    public OffsetDateTime getCalculadoEm() { return calculadoEm; }
    public void setCalculadoEm(OffsetDateTime v) { this.calculadoEm = v; }
    public String getRequisitanteId() { return requisitanteId; }

    public BigDecimal getTotalBruto() { return totalBruto; }
    public void setTotalBruto(BigDecimal v) { this.totalBruto = v; }
    public BigDecimal getTotalDesconto() { return totalDesconto; }
    public void setTotalDesconto(BigDecimal v) { this.totalDesconto = v; }
    public BigDecimal getTotalLiquido() { return totalLiquido; }
    public void setTotalLiquido(BigDecimal v) { this.totalLiquido = v; }
    public BigDecimal getTotalAbono() { return totalAbono; }
    public void setTotalAbono(BigDecimal v) { this.totalAbono = v; }
    public BigDecimal getTotal13() { return total13; }
    public void setTotal13(BigDecimal v) { this.total13 = v; }
}
