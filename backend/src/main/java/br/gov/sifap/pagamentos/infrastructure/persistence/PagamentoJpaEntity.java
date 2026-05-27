package br.gov.sifap.pagamentos.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "pagamento", schema = "pagamentos")
public class PagamentoJpaEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "ciclo_id", nullable = false)
    private UUID cicloId;

    @Column(name = "num_pagto", nullable = false, unique = true, insertable = false, updatable = false)
    private Long numPagto;

    @Column(name = "beneficiario_id", nullable = false)
    private UUID beneficiarioId;

    @Column(nullable = false, length = 11)
    private String cpf;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "valor_base", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorBase;

    @Column(name = "fator_regional", nullable = false, precision = 8, scale = 4)
    private BigDecimal fatorRegional;

    @Column(name = "fator_familiar", nullable = false, precision = 8, scale = 4)
    private BigDecimal fatorFamiliar;

    @Column(name = "fator_renda", nullable = false, precision = 8, scale = 4)
    private BigDecimal fatorRenda;

    @Column(name = "fator_idade", nullable = false, precision = 8, scale = 4)
    private BigDecimal fatorIdade;

    @Column(name = "fator_reajuste", nullable = false, precision = 8, scale = 4)
    private BigDecimal fatorReajuste;

    @Column(name = "valor_final", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorFinal;

    @Column(name = "calculado_em")
    private OffsetDateTime calculadoEm;

    @Column(name = "cod_programa", length = 36) private String codPrograma;
    @Column(length = 6) private String competencia;
    @Column(name = "vlr_desconto", nullable = false, precision = 15, scale = 2) private BigDecimal vlrDesconto = BigDecimal.ZERO;
    @Column(name = "vlr_liquido",  nullable = false, precision = 15, scale = 2) private BigDecimal vlrLiquido  = BigDecimal.ZERO;
    @Column(name = "vlr_abono",    nullable = false, precision = 15, scale = 2) private BigDecimal vlrAbono    = BigDecimal.ZERO;
    @Column(name = "vlr_13",       nullable = false, precision = 15, scale = 2) private BigDecimal vlr13       = BigDecimal.ZERO;
    @Column(name = "tipo_pgto", nullable = false, length = 1) private String tipoPgto = "N";
    @Column(name = "dt_geracao") private java.time.LocalDate dtGeracao;

    protected PagamentoJpaEntity() {}

    public PagamentoJpaEntity(UUID id, UUID cicloId, UUID beneficiarioId, String cpf,
                              String status, BigDecimal valorBase,
                              BigDecimal fatorRegional, BigDecimal fatorFamiliar,
                              BigDecimal fatorRenda, BigDecimal fatorIdade,
                              BigDecimal fatorReajuste, BigDecimal valorFinal,
                              OffsetDateTime calculadoEm) {
        this.id = id;
        this.cicloId = cicloId;
        this.beneficiarioId = beneficiarioId;
        this.cpf = cpf;
        this.status = status;
        this.valorBase = valorBase;
        this.fatorRegional = fatorRegional;
        this.fatorFamiliar = fatorFamiliar;
        this.fatorRenda = fatorRenda;
        this.fatorIdade = fatorIdade;
        this.fatorReajuste = fatorReajuste;
        this.valorFinal = valorFinal;
        this.calculadoEm = calculadoEm;
    }

    public UUID getId() { return id; }
    public UUID getCicloId() { return cicloId; }
    public Long getNumPagto() { return numPagto; }
    public UUID getBeneficiarioId() { return beneficiarioId; }
    public String getCpf() { return cpf; }
    public String getStatus() { return status; }
    public BigDecimal getValorBase() { return valorBase; }
    public BigDecimal getValorFinal() { return valorFinal; }
    public BigDecimal getFatorRegional() { return fatorRegional; }
    public BigDecimal getFatorFamiliar() { return fatorFamiliar; }
    public BigDecimal getFatorRenda() { return fatorRenda; }
    public BigDecimal getFatorIdade() { return fatorIdade; }
    public BigDecimal getFatorReajuste() { return fatorReajuste; }
    public OffsetDateTime getCalculadoEm() { return calculadoEm; }

    public String getCodPrograma() { return codPrograma; }
    public void setCodPrograma(String v) { this.codPrograma = v; }
    public String getCompetencia() { return competencia; }
    public void setCompetencia(String v) { this.competencia = v; }
    public BigDecimal getVlrDesconto() { return vlrDesconto; }
    public void setVlrDesconto(BigDecimal v) { this.vlrDesconto = v; }
    public BigDecimal getVlrLiquido() { return vlrLiquido; }
    public void setVlrLiquido(BigDecimal v) { this.vlrLiquido = v; }
    public BigDecimal getVlrAbono() { return vlrAbono; }
    public void setVlrAbono(BigDecimal v) { this.vlrAbono = v; }
    public BigDecimal getVlr13() { return vlr13; }
    public void setVlr13(BigDecimal v) { this.vlr13 = v; }
    public String getTipoPgto() { return tipoPgto; }
    public void setTipoPgto(String v) { this.tipoPgto = v; }
    public java.time.LocalDate getDtGeracao() { return dtGeracao; }
    public void setDtGeracao(java.time.LocalDate v) { this.dtGeracao = v; }
    public void setStatus(String v) { this.status = v; }
}
