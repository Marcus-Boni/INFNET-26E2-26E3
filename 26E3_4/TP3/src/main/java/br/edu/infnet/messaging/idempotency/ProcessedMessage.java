package br.edu.infnet.messaging.idempotency;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade que registra o identificador único de cada mensagem já processada
 * com sucesso, servindo como barreira de idempotência no banco de dados.
 */
public class ProcessedMessage {

    private final UUID messageId;
    private final String messageType;
    private final Instant processedAt;

    public ProcessedMessage(UUID messageId, String messageType) {
        this.messageId = Objects.requireNonNull(messageId, "Message ID é obrigatório");
        this.messageType = Objects.requireNonNull(messageType, "Message Type é obrigatório");
        this.processedAt = Instant.now();
    }

    public UUID getMessageId() {
        return messageId;
    }

    public String getMessageType() {
        return messageType;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}
