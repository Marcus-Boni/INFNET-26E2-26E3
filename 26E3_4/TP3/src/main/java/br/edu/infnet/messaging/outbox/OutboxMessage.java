package br.edu.infnet.messaging.outbox;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade que representa uma mensagem/evento armazenado na tabela Outbox.
 * Gravada de forma atômica junto às entidades de negócio na mesma transação ACID do banco.
 */
public class OutboxMessage {

    public enum Status {
        PENDING, PUBLISHED, FAILED
    }

    private final UUID id;
    private final String aggregateType;
    private final UUID aggregateId;
    private final String eventType;
    private final String payload;
    private final Instant createdAt;
    private Status status;

    public OutboxMessage(String aggregateType, UUID aggregateId, String eventType, String payload) {
        this.id = UUID.randomUUID();
        this.aggregateType = Objects.requireNonNull(aggregateType, "Tipo de agregado é obrigatório");
        this.aggregateId = Objects.requireNonNull(aggregateId, "ID do agregado é obrigatório");
        this.eventType = Objects.requireNonNull(eventType, "Tipo de evento é obrigatório");
        this.payload = Objects.requireNonNull(payload, "Payload é obrigatório");
        this.createdAt = Instant.now();
        this.status = Status.PENDING;
    }

    public UUID getId() {
        return id;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public UUID getAggregateId() {
        return aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Status getStatus() {
        return status;
    }

    public void markAsPublished() {
        this.status = Status.PUBLISHED;
    }

    public void markAsFailed() {
        this.status = Status.FAILED;
    }
}
