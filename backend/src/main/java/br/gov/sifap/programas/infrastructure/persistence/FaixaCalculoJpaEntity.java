package br.gov.sifap.programas.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "faixa_calculo", schema = "programas")
public class FaixaCalculoJpaEntity {
    @Id private UUID id;
    @Column(name = "programa_codigo", nullable = false, length = 36) private String programaCodigo;
    @Column(nullable = false) private Integer ordem;
    @Column(name = "renda_ate", nullable = false, precision = 9, scale = 2) private BigDecimal rendaAte;
    @Column(nullable = false, precision = 7, scale = 4) private BigDecimal fator;

    protected FaixaCalculoJpaEntity() {}
    public FaixaCalculoJpaEntity(UUID id, String programaCodigo, Integer ordem, BigDecimal rendaAte, BigDecimal fator) {
        this.id = id; this.programaCodigo = programaCodigo; this.ordem = ordem;
        this.rendaAte = rendaAte; this.fator = fator;
    }
    public UUID getId() { return id; }
    public String getProgramaCodigo() { return programaCodigo; }
    public Integer getOrdem() { return ordem; }
    public BigDecimal getRendaAte() { return rendaAte; }
    public BigDecimal getFator() { return fator; }
}
