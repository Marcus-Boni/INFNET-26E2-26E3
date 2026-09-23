package br.com.freela.auditoria;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaController.class);
    private final EventoAuditoriaRepository repository;

    public AuditoriaController(EventoAuditoriaRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<EventoAuditoria> listar(
            @RequestHeader(value = "X-Correlation-Id", required = false) String headerCorrelationId,
            @RequestParam(value = "contratoId", required = false) UUID contratoId,
            @RequestParam(value = "correlationId", required = false) String correlationIdParam,
            @RequestParam(value = "eventId", required = false) UUID eventId
    ) {
        String corrId = headerCorrelationId != null ? headerCorrelationId : correlationIdParam;
        if (corrId != null) {
            MDC.put("correlationId", corrId);
        }
        try {
            log.info("http.auditoria.listar correlationId={} contratoId={} eventId={}", corrId, contratoId, eventId);

            if (eventId != null) {
                return repository.findByEventId(eventId).map(List::of).orElseGet(List::of);
            }
            if (contratoId != null) {
                return repository.findByAggregateIdOrderByRecebidoEmAsc(contratoId);
            }
            if (correlationIdParam != null && !correlationIdParam.isBlank()) {
                return repository.findByCorrelationIdOrderByRecebidoEmAsc(correlationIdParam);
            }
            return repository.findAll();
        } finally {
            MDC.clear();
        }
    }
}
