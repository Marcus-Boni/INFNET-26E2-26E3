package br.com.freela.notificacao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "mensagens_processadas")
public class MensagemProcessada {

    @Id
    private UUID eventId;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String consumerName;

    @Column(nullable = false)
    private Instant processadoEm;

    protected MensagemProcessada() {}

    public MensagemProcessada(UUID eventId, String eventType, String consumerName) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.consumerName = consumerName;
        this.processadoEm = Instant.now();
    }

    public UUID getEventId() { return eventId; }
    public String getEventType() { return eventType; }
    public String getConsumerName() { return consumerName; }
    public Instant getProcessadoEm() { return processadoEm; }
}
