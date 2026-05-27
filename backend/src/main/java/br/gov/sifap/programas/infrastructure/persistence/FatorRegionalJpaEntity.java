package br.gov.sifap.programas.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "fator_regional", schema = "programas")
public class FatorRegionalJpaEntity {
    @Id
    private Integer regiao;
    @Column(nullable = false, precision = 8, scale = 4)
    private BigDecimal fator;

    protected FatorRegionalJpaEntity() {}
    public FatorRegionalJpaEntity(Integer regiao, BigDecimal fator) {
        this.regiao = regiao; this.fator = fator;
    }
    public Integer getRegiao() { return regiao; }
    public BigDecimal getFator() { return fator; }
}
