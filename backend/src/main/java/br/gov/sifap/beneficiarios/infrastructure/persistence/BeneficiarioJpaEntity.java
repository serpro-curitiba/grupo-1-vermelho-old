package br.gov.sifap.beneficiarios.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entidade JPA do beneficiário — mapeia todas as colunas do DDM BENEFICIARIO
 * (ARQ 150), incluindo as adicionadas em V2.
 */
@Entity
@Table(name = "beneficiario", schema = "beneficiarios")
public class BeneficiarioJpaEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 11, unique = true)
    private String cpf;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false)
    private Integer regiao;

    @Column(name = "renda_mensal", nullable = false, precision = 15, scale = 2)
    private BigDecimal rendaMensal;

    @Column(nullable = false)
    private Integer idade;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "programa_codigo", nullable = false, length = 36)
    private String programaCodigo;

    @Column(name = "num_inscricao")
    private Long numInscricao;

    @Column(name = "nome_mae", length = 60)   private String nomeMae;
    @Column(name = "nome_pai", length = 60)   private String nomePai;
    @Column(name = "dt_nascimento")           private LocalDate dtNascimento;
    @Column(length = 1)                       private String sexo;
    @Column(name = "est_civil", length = 1)   private String estCivil;
    @Column(name = "rg_numero", length = 15)  private String rgNumero;
    @Column(name = "rg_orgao",  length = 10)  private String rgOrgao;
    @Column(name = "rg_uf",     length = 2)   private String rgUf;
    @Column(length = 60) private String logradouro;
    @Column(length = 10) private String numero;
    @Column(length = 30) private String complemento;
    @Column(length = 40) private String bairro;
    @Column(length = 40) private String municipio;
    @Column(length = 2)  private String uf;
    @Column(length = 8)  private String cep;
    @Column(name = "tel_fixo",    length = 14) private String telFixo;
    @Column(name = "tel_celular", length = 15) private String telCelular;
    @Column(length = 80) private String email;
    @Column(length = 11) private String nis;
    @Column(name = "dt_cadastro")     private LocalDate dtCadastro;
    @Column(name = "dt_inicio_benef") private LocalDate dtInicioBenef;
    @Column(name = "dt_fim_benef")    private LocalDate dtFimBenef;
    @Column(name = "mot_situacao", length = 3) private String motSituacao;
    @Column(name = "documentos_ok", nullable = false, length = 1) private String documentosOk = "N";
    @Column(name = "ind_biometria", nullable = false, length = 1) private String indBiometria = "N";
    @Column(name = "cod_elegibilidade", length = 5) private String codElegibilidade;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm;
    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime atualizadoEm;

    protected BeneficiarioJpaEntity() {}

    public BeneficiarioJpaEntity(UUID id, String cpf, String nome, Integer regiao,
                                 BigDecimal rendaMensal, Integer idade, String status,
                                 String programaCodigo, OffsetDateTime agora) {
        this.id = id; this.cpf = cpf; this.nome = nome; this.regiao = regiao;
        this.rendaMensal = rendaMensal; this.idade = idade; this.status = status;
        this.programaCodigo = programaCodigo;
        this.criadoEm = agora; this.atualizadoEm = agora;
    }

    public UUID getId() { return id; }
    public String getCpf() { return cpf; }
    public String getNome() { return nome; }
    public Integer getRegiao() { return regiao; }
    public BigDecimal getRendaMensal() { return rendaMensal; }
    public Integer getIdade() { return idade; }
    public String getStatus() { return status; }
    public String getProgramaCodigo() { return programaCodigo; }
    public Long getNumInscricao() { return numInscricao; }
    public String getNomeMae() { return nomeMae; }
    public String getNomePai() { return nomePai; }
    public LocalDate getDtNascimento() { return dtNascimento; }
    public String getSexo() { return sexo; }
    public String getEstCivil() { return estCivil; }
    public String getRgNumero() { return rgNumero; }
    public String getRgOrgao() { return rgOrgao; }
    public String getRgUf() { return rgUf; }
    public String getLogradouro() { return logradouro; }
    public String getNumero() { return numero; }
    public String getComplemento() { return complemento; }
    public String getBairro() { return bairro; }
    public String getMunicipio() { return municipio; }
    public String getUf() { return uf; }
    public String getCep() { return cep; }
    public String getTelFixo() { return telFixo; }
    public String getTelCelular() { return telCelular; }
    public String getEmail() { return email; }
    public String getNis() { return nis; }
    public LocalDate getDtCadastro() { return dtCadastro; }
    public LocalDate getDtInicioBenef() { return dtInicioBenef; }
    public LocalDate getDtFimBenef() { return dtFimBenef; }
    public String getMotSituacao() { return motSituacao; }
    public String getDocumentosOk() { return documentosOk; }
    public String getIndBiometria() { return indBiometria; }
    public String getCodElegibilidade() { return codElegibilidade; }
    public OffsetDateTime getCriadoEm() { return criadoEm; }
    public OffsetDateTime getAtualizadoEm() { return atualizadoEm; }

    public void setNome(String v) { this.nome = v; }
    public void setRegiao(Integer v) { this.regiao = v; }
    public void setRendaMensal(BigDecimal v) { this.rendaMensal = v; }
    public void setIdade(Integer v) { this.idade = v; }
    public void setStatus(String v) { this.status = v; }
    public void setProgramaCodigo(String v) { this.programaCodigo = v; }
    public void setNumInscricao(Long v) { this.numInscricao = v; }
    public void setNomeMae(String v) { this.nomeMae = v; }
    public void setNomePai(String v) { this.nomePai = v; }
    public void setDtNascimento(LocalDate v) { this.dtNascimento = v; }
    public void setSexo(String v) { this.sexo = v; }
    public void setEstCivil(String v) { this.estCivil = v; }
    public void setRgNumero(String v) { this.rgNumero = v; }
    public void setRgOrgao(String v) { this.rgOrgao = v; }
    public void setRgUf(String v) { this.rgUf = v; }
    public void setLogradouro(String v) { this.logradouro = v; }
    public void setNumero(String v) { this.numero = v; }
    public void setComplemento(String v) { this.complemento = v; }
    public void setBairro(String v) { this.bairro = v; }
    public void setMunicipio(String v) { this.municipio = v; }
    public void setUf(String v) { this.uf = v; }
    public void setCep(String v) { this.cep = v; }
    public void setTelFixo(String v) { this.telFixo = v; }
    public void setTelCelular(String v) { this.telCelular = v; }
    public void setEmail(String v) { this.email = v; }
    public void setNis(String v) { this.nis = v; }
    public void setDtCadastro(LocalDate v) { this.dtCadastro = v; }
    public void setDtInicioBenef(LocalDate v) { this.dtInicioBenef = v; }
    public void setDtFimBenef(LocalDate v) { this.dtFimBenef = v; }
    public void setMotSituacao(String v) { this.motSituacao = v; }
    public void setDocumentosOk(String v) { this.documentosOk = v; }
    public void setIndBiometria(String v) { this.indBiometria = v; }
    public void setCodElegibilidade(String v) { this.codElegibilidade = v; }
    public void setAtualizadoEm(OffsetDateTime v) { this.atualizadoEm = v; }
}
