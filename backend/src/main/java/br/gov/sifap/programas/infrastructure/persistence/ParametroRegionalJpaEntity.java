package br.gov.sifap.programas.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "parametro_regional", schema = "programas")
public class ParametroRegionalJpaEntity {
    @Id private UUID id;
    @Column(name = "programa_codigo", nullable = false, length = 36) private String programaCodigo;
    @Column(nullable = false) private Integer regiao;
    @Column(name = "fator_extra", nullable = false, precision = 7, scale = 4) private BigDecimal fatorExtra;

    protected ParametroRegionalJpaEntity() {}
    public ParametroRegionalJpaEntity(UUID id, String programaCodigo, Integer regiao, BigDecimal fatorExtra) {
        this.id = id; this.programaCodigo = programaCodigo; this.regiao = regiao; this.fatorExtra = fatorExtra;
    }
    public UUID getId() { return id; }
    public String getProgramaCodigo() { return programaCodigo; }
    public Integer getRegiao() { return regiao; }
    public BigDecimal getFatorExtra() { return fatorExtra; }
}
