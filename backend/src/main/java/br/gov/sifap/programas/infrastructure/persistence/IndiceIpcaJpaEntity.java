package br.gov.sifap.programas.infrastructure.persistence;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "indice_ipca", schema = "programas")
@IdClass(IndiceIpcaJpaEntity.PK.class)
public class IndiceIpcaJpaEntity {

    @Id private Integer ano;
    @Id private Integer mes;
    @Column(nullable = false, precision = 8, scale = 6) private BigDecimal fator;

    protected IndiceIpcaJpaEntity() {}
    public IndiceIpcaJpaEntity(Integer ano, Integer mes, BigDecimal fator) {
        this.ano = ano; this.mes = mes; this.fator = fator;
    }
    public Integer getAno() { return ano; }
    public Integer getMes() { return mes; }
    public BigDecimal getFator() { return fator; }

    public static class PK implements Serializable {
        private Integer ano;
        private Integer mes;
        public PK() {}
        public PK(Integer ano, Integer mes) { this.ano = ano; this.mes = mes; }
        @Override public boolean equals(Object o) {
            if (!(o instanceof PK p)) return false;
            return Objects.equals(ano, p.ano) && Objects.equals(mes, p.mes);
        }
        @Override public int hashCode() { return Objects.hash(ano, mes); }
    }
}
