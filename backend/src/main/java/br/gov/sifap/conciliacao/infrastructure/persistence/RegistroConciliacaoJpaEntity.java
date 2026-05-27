package br.gov.sifap.conciliacao.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "registro", schema = "conciliacao")
public class RegistroConciliacaoJpaEntity {

    @Id private UUID id;
    @Column(name = "arquivo_id", nullable = false) private UUID arquivoId;
    @Column(nullable = false, length = 11) private String cpf;
    @Column(name = "num_pagto") private Long numPagto;
    @Column(name = "vlr_retorno", nullable = false, precision = 15, scale = 2) private BigDecimal vlrRetorno;
    @Column(name = "dt_pagamento") private LocalDate dtPagamento;
    @Column(name = "cod_retorno", length = 2) private String codRetorno;
    @Column(nullable = false, length = 20) private String resultado;
    @Column(precision = 15, scale = 2) private BigDecimal diferenca;

    protected RegistroConciliacaoJpaEntity() {}

    public RegistroConciliacaoJpaEntity(UUID id, UUID arquivoId, String cpf, Long numPagto,
                                         BigDecimal vlrRetorno, LocalDate dtPagamento,
                                         String codRetorno, String resultado, BigDecimal diferenca) {
        this.id = id; this.arquivoId = arquivoId; this.cpf = cpf; this.numPagto = numPagto;
        this.vlrRetorno = vlrRetorno; this.dtPagamento = dtPagamento;
        this.codRetorno = codRetorno; this.resultado = resultado; this.diferenca = diferenca;
    }

    public UUID getId() { return id; }
    public UUID getArquivoId() { return arquivoId; }
    public String getCpf() { return cpf; }
    public Long getNumPagto() { return numPagto; }
    public BigDecimal getVlrRetorno() { return vlrRetorno; }
    public LocalDate getDtPagamento() { return dtPagamento; }
    public String getCodRetorno() { return codRetorno; }
    public String getResultado() { return resultado; }
    public BigDecimal getDiferenca() { return diferenca; }
}
