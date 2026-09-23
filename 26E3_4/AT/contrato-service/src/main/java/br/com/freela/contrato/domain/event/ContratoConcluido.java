package br.com.freela.contrato.domain.event;

import br.com.freela.contrato.domain.model.Contrato;
import br.com.freela.contrato.domain.shared.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ContratoConcluido(
        UUID eventId,
        Instant occurredAt,
        String eventType,
        UUID contratoId,
        UUID clienteId,
        UUID freelancerId,
        String titulo,
        BigDecimal valor,
        String correlationId
) implements DomainEvent {

    public static ContratoConcluido novo(Contrato c) {
        return novo(c, UUID.randomUUID().toString());
    }

    public static ContratoConcluido novo(Contrato c, String correlationId) {
        return new ContratoConcluido(
                UUID.randomUUID(),
                Instant.now(),
                "ContratoConcluido",
                c.id(),
                c.clienteId(),
                c.freelancerId(),
                c.titulo(),
                c.valor(),
                correlationId != null ? correlationId : UUID.randomUUID().toString()
        );
    }
}
