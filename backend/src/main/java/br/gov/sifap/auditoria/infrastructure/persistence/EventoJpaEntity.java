package br.gov.sifap.auditoria.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "evento", schema = "auditoria")
public class EventoJpaEntity {

    @Id private UUID id;
    @Column(nullable = false, length = 100) private String tipo;
    @Column(nullable = false, length = 100) private String agregado;
    @Column(name = "agregado_id", nullable = false, length = 64) private String agregadoId;
    @Column(name = "usuario_id", nullable = false, length = 64) private String usuarioId;
    @Column(name = "payload_json", nullable = false, columnDefinition = "TEXT") private String payloadJson;
    @Column(name = "ocorrido_em", nullable = false) private OffsetDateTime ocorridoEm;
    @Column(length = 2) private String acao;
    @Column(name = "ip_origem", length = 45) private String ipOrigem;
    @Column(name = "id_correlacao", length = 36) private String idCorrelacao;
    @Column(nullable = false, length = 1) private String sucesso = "S";

    protected EventoJpaEntity() {}

    public EventoJpaEntity(UUID id, String tipo, String agregado, String agregadoId,
                           String usuarioId, String payloadJson, OffsetDateTime ocorridoEm,
                           String acao, String ipOrigem, String idCorrelacao, String sucesso) {
        this.id = id; this.tipo = tipo; this.agregado = agregado; this.agregadoId = agregadoId;
        this.usuarioId = usuarioId; this.payloadJson = payloadJson; this.ocorridoEm = ocorridoEm;
        this.acao = acao; this.ipOrigem = ipOrigem; this.idCorrelacao = idCorrelacao;
        this.sucesso = sucesso == null ? "S" : sucesso;
    }

    public UUID getId() { return id; }
    public String getTipo() { return tipo; }
    public String getAgregado() { return agregado; }
    public String getAgregadoId() { return agregadoId; }
    public String getUsuarioId() { return usuarioId; }
    public String getPayloadJson() { return payloadJson; }
    public OffsetDateTime getOcorridoEm() { return ocorridoEm; }
    public String getAcao() { return acao; }
    public String getIpOrigem() { return ipOrigem; }
    public String getIdCorrelacao() { return idCorrelacao; }
    public String getSucesso() { return sucesso; }
}
