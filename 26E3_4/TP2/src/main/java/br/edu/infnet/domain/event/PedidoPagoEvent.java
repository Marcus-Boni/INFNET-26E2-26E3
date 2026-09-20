package br.edu.infnet.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Questão 10: Implementação concreta de um Evento de Domínio.
 * 
 * Implementado como record Java, garantindo imutabilidade nata,
 * métodos equals/hashCode automáticos e clareza de dados.
 */
public record PedidoPagoEvent(
    UUID eventId,
    Instant occurredOn,
    UUID pedidoId,
    UUID clienteId,
    BigDecimal valorPago
) implements DomainEvent {

    /**
     * Construtor de conveniência que gera id e timestamp automaticamente no momento da criação do evento.
     */
    public PedidoPagoEvent(UUID pedidoId, UUID clienteId, BigDecimal valorPago) {
        this(UUID.randomUUID(), Instant.now(), pedidoId, clienteId, valorPago);
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public Instant getOccurredOn() {
        return occurredOn;
    }
}
