package br.gov.sifap.programas.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "programa", schema = "programas")
public class ProgramaJpaEntity {

    @Id
    @Column(length = 36)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "valor_base", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorBase;

    @Column(length = 10) private String sigla;
    @Column(nullable = false, length = 1) private String tipo = "A";
    @Column(name = "cod_elegibilidade", length = 5) private String codElegibilidade;
    @Column(name = "dt_inicio") private LocalDate dtInicio;
    @Column(name = "dt_fim") private LocalDate dtFim;
    @Column(name = "renda_max", precision = 9, scale = 2) private BigDecimal rendaMax;
    @Column(name = "idade_min") private Integer idadeMin;
    @Column(name = "idade_max") private Integer idadeMax;
    @Column(name = "pct_reajuste_anual", nullable = false, precision = 7, scale = 4)
    private BigDecimal pctReajusteAnual = BigDecimal.ZERO;
    @Column(name = "fator_k", nullable = false, precision = 7, scale = 4)
    private BigDecimal fatorK = BigDecimal.ONE;
    @Column(name = "vlr_calc_ajustado", precision = 9, scale = 2)
    private BigDecimal vlrCalcAjustado;

    protected ProgramaJpaEntity() {}

    public ProgramaJpaEntity(String codigo, String nome, String status, BigDecimal valorBase, String tipo) {
        this.codigo = codigo; this.nome = nome; this.status = status;
        this.valorBase = valorBase; this.tipo = tipo;
    }

    public String getCodigo() { return codigo; }
    public String getNome() { return nome; }
    public String getStatus() { return status; }
    public BigDecimal getValorBase() { return valorBase; }
    public String getSigla() { return sigla; }
    public String getTipo() { return tipo; }
    public String getCodElegibilidade() { return codElegibilidade; }
    public LocalDate getDtInicio() { return dtInicio; }
    public LocalDate getDtFim() { return dtFim; }
    public BigDecimal getRendaMax() { return rendaMax; }
    public Integer getIdadeMin() { return idadeMin; }
    public Integer getIdadeMax() { return idadeMax; }
    public BigDecimal getPctReajusteAnual() { return pctReajusteAnual; }
    public BigDecimal getFatorK() { return fatorK; }
    public BigDecimal getVlrCalcAjustado() { return vlrCalcAjustado; }

    public void setNome(String v) { this.nome = v; }
    public void setStatus(String v) { this.status = v; }
    public void setValorBase(BigDecimal v) { this.valorBase = v; }
    public void setSigla(String v) { this.sigla = v; }
    public void setTipo(String v) { this.tipo = v; }
    public void setCodElegibilidade(String v) { this.codElegibilidade = v; }
    public void setDtInicio(LocalDate v) { this.dtInicio = v; }
    public void setDtFim(LocalDate v) { this.dtFim = v; }
    public void setRendaMax(BigDecimal v) { this.rendaMax = v; }
    public void setIdadeMin(Integer v) { this.idadeMin = v; }
    public void setIdadeMax(Integer v) { this.idadeMax = v; }
    public void setPctReajusteAnual(BigDecimal v) { this.pctReajusteAnual = v; }
    public void setFatorK(BigDecimal v) { this.fatorK = v; }
    public void setVlrCalcAjustado(BigDecimal v) { this.vlrCalcAjustado = v; }
}
