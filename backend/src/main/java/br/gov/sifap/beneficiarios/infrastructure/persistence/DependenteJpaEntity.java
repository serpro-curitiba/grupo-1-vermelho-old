package br.gov.sifap.beneficiarios.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "dependente", schema = "beneficiarios")
public class DependenteJpaEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "beneficiario_id", nullable = false)
    private UUID beneficiarioId;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false)
    private Integer idade;

    @Column(length = 11)
    private String cpf;

    @Column(name = "dt_nascimento")
    private LocalDate dtNascimento;

    @Column(length = 2)
    private String parentesco;

    @Column(length = 15)
    private String documento;

    @Column(length = 1)
    private String sexo;

    @Column(name = "ind_deficiencia", nullable = false, length = 1)
    private String indDeficiencia = "N";

    @Column(nullable = false, length = 20)
    private String situacao = "ATIVO";

    protected DependenteJpaEntity() {}

    public DependenteJpaEntity(UUID id, UUID beneficiarioId, String nome, Integer idade,
                               String cpf, LocalDate dtNascimento, String parentesco,
                               String documento, String sexo, String indDeficiencia) {
        this.id = id; this.beneficiarioId = beneficiarioId;
        this.nome = nome; this.idade = idade;
        this.cpf = cpf; this.dtNascimento = dtNascimento;
        this.parentesco = parentesco; this.documento = documento;
        this.sexo = sexo;
        this.indDeficiencia = indDeficiencia == null ? "N" : indDeficiencia;
    }

    public UUID getId() { return id; }
    public UUID getBeneficiarioId() { return beneficiarioId; }
    public String getNome() { return nome; }
    public Integer getIdade() { return idade; }
    public String getCpf() { return cpf; }
    public LocalDate getDtNascimento() { return dtNascimento; }
    public String getParentesco() { return parentesco; }
    public String getDocumento() { return documento; }
    public String getSexo() { return sexo; }
    public String getIndDeficiencia() { return indDeficiencia; }
    public String getSituacao() { return situacao; }

    public void setSituacao(String v) { this.situacao = v; }
}
