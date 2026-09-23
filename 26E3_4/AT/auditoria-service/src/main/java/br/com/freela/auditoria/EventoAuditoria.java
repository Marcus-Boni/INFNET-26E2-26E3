package br.com.freela.auditoria;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auditoria_eventos", indexes = {
        @Index(name = "idx_auditoria_event_id", columnList = "eventId", unique = true),
        @Index(name = "idx_auditoria_aggregate_id", columnList = "aggregateId"),
        @Index(name = "idx_auditoria_correlation_id", columnList = "correlationId")
})
public class EventoAuditoria {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID eventId;

    @Column(nullable = false)
    private UUID aggregateId;

    @Column(nullable = false)
    private String eventType;

    private String correlationId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;

    private Instant occurredAt;

    @Column(nullable = false)
    private Instant recebidoEm;

    protected EventoAuditoria() {}

    public EventoAuditoria(UUID eventId, UUID aggregateId, String eventType, String correlationId, String payload, Instant occurredAt) {
        this.id = UUID.randomUUID();
        this.eventId = eventId;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.correlationId = correlationId;
        this.payload = payload;
        this.occurredAt = occurredAt != null ? occurredAt : Instant.now();
        this.recebidoEm = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getEventId() { return eventId; }
    public UUID getAggregateId() { return aggregateId; }
    public String getEventType() { return eventType; }
    public String getCorrelationId() { return correlationId; }
    public String getPayload() { return payload; }
    public Instant getOccurredAt() { return occurredAt; }
    public Instant getRecebidoEm() { return recebidoEm; }
}
