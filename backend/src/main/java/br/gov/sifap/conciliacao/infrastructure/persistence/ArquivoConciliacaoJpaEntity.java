package br.gov.sifap.conciliacao.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "arquivo", schema = "conciliacao")
public class ArquivoConciliacaoJpaEntity {

    @Id private UUID id;
    @Column(name = "nome_arquivo", nullable = false, length = 120) private String nomeArquivo;
    @Column(nullable = false, length = 6) private String competencia;
    @Column(name = "importado_em", nullable = false) private OffsetDateTime importadoEm;
    @Column(name = "qtd_lidos", nullable = false) private int qtdLidos;
    @Column(name = "qtd_conciliados", nullable = false) private int qtdConciliados;
    @Column(name = "qtd_divergentes", nullable = false) private int qtdDivergentes;
    @Column(name = "qtd_nao_encontrados", nullable = false) private int qtdNaoEncontrados;
    @Column(length = 64) private String sha256;

    protected ArquivoConciliacaoJpaEntity() {}

    public ArquivoConciliacaoJpaEntity(UUID id, String nome, String competencia, OffsetDateTime importado, String sha256) {
        this.id = id; this.nomeArquivo = nome; this.competencia = competencia;
        this.importadoEm = importado; this.sha256 = sha256;
    }

    public UUID getId() { return id; }
    public String getNomeArquivo() { return nomeArquivo; }
    public String getCompetencia() { return competencia; }
    public OffsetDateTime getImportadoEm() { return importadoEm; }
    public int getQtdLidos() { return qtdLidos; }
    public int getQtdConciliados() { return qtdConciliados; }
    public int getQtdDivergentes() { return qtdDivergentes; }
    public int getQtdNaoEncontrados() { return qtdNaoEncontrados; }
    public String getSha256() { return sha256; }

    public void setQtdLidos(int v) { this.qtdLidos = v; }
    public void setQtdConciliados(int v) { this.qtdConciliados = v; }
    public void setQtdDivergentes(int v) { this.qtdDivergentes = v; }
    public void setQtdNaoEncontrados(int v) { this.qtdNaoEncontrados = v; }
}
