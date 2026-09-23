package br.com.freela.contrato.domain.event;

import br.com.freela.contrato.domain.model.Contrato;
import br.com.freela.contrato.domain.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record ContratoCancelado(
        UUID eventId,
        Instant occurredAt,
        String eventType,
        UUID contratoId,
        UUID clienteId,
        UUID freelancerId,
        String titulo,
        String correlationId
) implements DomainEvent {

    public static ContratoCancelado novo(Contrato c) {
        return novo(c, UUID.randomUUID().toString());
    }

    public static ContratoCancelado novo(Contrato c, String correlationId) {
        return new ContratoCancelado(
                UUID.randomUUID(),
                Instant.now(),
                "ContratoCancelado",
                c.id(),
                c.clienteId(),
                c.freelancerId(),
                c.titulo(),
                correlationId != null ? correlationId : UUID.randomUUID().toString()
        );
    }
}
