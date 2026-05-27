package br.gov.sifap.auditoria.infrastructure.persistence;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "evento_campo_alterado", schema = "auditoria")
public class EventoCampoAlteradoJpaEntity {

    @Id private UUID id;
    @Column(name = "evento_id", nullable = false) private UUID eventoId;
    @Column(nullable = false) private int ordem;
    @Column(nullable = false, length = 50) private String campo;
    @Column(name = "valor_ant", length = 200) private String valorAnt;
    @Column(name = "valor_pos", length = 200) private String valorPos;

    protected EventoCampoAlteradoJpaEntity() {}

    public EventoCampoAlteradoJpaEntity(UUID id, UUID eventoId, int ordem,
                                         String campo, String valorAnt, String valorPos) {
        this.id = id; this.eventoId = eventoId; this.ordem = ordem;
        this.campo = campo; this.valorAnt = valorAnt; this.valorPos = valorPos;
    }

    public UUID getId() { return id; }
    public UUID getEventoId() { return eventoId; }
    public int getOrdem() { return ordem; }
    public String getCampo() { return campo; }
    public String getValorAnt() { return valorAnt; }
    public String getValorPos() { return valorPos; }
}
