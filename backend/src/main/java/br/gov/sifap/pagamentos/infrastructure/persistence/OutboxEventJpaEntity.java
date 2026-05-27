package br.gov.sifap.pagamentos.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_event", schema = "pagamentos")
public class OutboxEventJpaEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String topico;

    @Column(nullable = false, length = 100)
    private String tipo;

    @Column(name = "payload_json", nullable = false, columnDefinition = "text")
    private String payloadJson;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm;

    @Column(name = "publicado_em")
    private OffsetDateTime publicadoEm;

    @Column(nullable = false, length = 20)
    private String status = "PENDENTE";

    protected OutboxEventJpaEntity() {}

    public OutboxEventJpaEntity(UUID id, String topico, String tipo, String payloadJson,
                                OffsetDateTime criadoEm) {
        this.id = id;
        this.topico = topico;
        this.tipo = tipo;
        this.payloadJson = payloadJson;
        this.criadoEm = criadoEm;
    }

    public UUID getId() { return id; }
    public String getTopico() { return topico; }
    public String getTipo() { return tipo; }
    public String getPayloadJson() { return payloadJson; }
    public OffsetDateTime getCriadoEm() { return criadoEm; }
    public String getStatus() { return status; }
}
