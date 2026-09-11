package br.edu.infnet.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Questão 9: Abstração de um Evento de Domínio.
 * 
 * Contrato base para todos os eventos de domínio da aplicação.
 * Garante que todo evento possua identificador único imutável e carimbo de data/hora da ocorrência.
 */
public interface DomainEvent {

    /**
     * Identificador único do evento para rastreabilidade e idempotência.
     */
    UUID getEventId();

    /**
     * Momento exato em que o fato de negócio ocorreu no domínio.
     */
    Instant getOccurredOn();
}
