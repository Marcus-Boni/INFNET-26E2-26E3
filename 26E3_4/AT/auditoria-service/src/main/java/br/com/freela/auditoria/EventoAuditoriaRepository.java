package br.com.freela.auditoria;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventoAuditoriaRepository extends JpaRepository<EventoAuditoria, UUID> {
    boolean existsByEventId(UUID eventId);
    Optional<EventoAuditoria> findByEventId(UUID eventId);
    List<EventoAuditoria> findByAggregateIdOrderByRecebidoEmAsc(UUID aggregateId);
    List<EventoAuditoria> findByCorrelationIdOrderByRecebidoEmAsc(String correlationId);
}
