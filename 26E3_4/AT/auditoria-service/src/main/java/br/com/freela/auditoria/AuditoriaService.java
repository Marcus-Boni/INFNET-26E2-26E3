package br.com.freela.auditoria;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuditoriaService {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaService.class);
    private final EventoAuditoriaRepository repository;

    public AuditoriaService(EventoAuditoriaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void registrar(UUID eventId, UUID aggregateId, String eventType, String correlationId, String payload, Instant occurredAt) {
        log.info("auditoria.registro.inicio eventId={} aggregateId={} eventType={} correlationId={}",
                eventId, aggregateId, eventType, correlationId);

        // Idempotência: caso a mensagem seja reprocessada, não duplicar registro
        if (repository.existsByEventId(eventId)) {
            log.warn("auditoria.registro.duplicada.ignorada eventId={} aggregateId={} eventType={} resultado=IGNORADO_DUPLICADO",
                    eventId, aggregateId, eventType);
            return;
        }

        var evento = repository.save(new EventoAuditoria(eventId, aggregateId, eventType, correlationId, payload, occurredAt));
        log.info("auditoria.registro.sucesso auditoriaId={} eventId={} aggregateId={} eventType={}",
                evento.getId(), eventId, aggregateId, eventType);
    }
}
