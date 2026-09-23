package br.com.freela.auditoria;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class AuditoriaKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaKafkaConsumer.class);

    private final AuditoriaService auditoriaService;
    private final ObjectMapper objectMapper;

    public AuditoriaKafkaConsumer(AuditoriaService auditoriaService, ObjectMapper objectMapper) {
        this.auditoriaService = auditoriaService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.kafka.topics.contratos:contratos.v1.events}", groupId = "auditoria-group")
    public void consumir(ConsumerRecord<String, String> record) {
        try {
            JsonNode root = objectMapper.readTree(record.value());

            UUID eventId = UUID.fromString(root.path("eventId").asText());
            String eventType = root.path("eventType").asText();
            UUID aggregateId = UUID.fromString(root.path("contratoId").asText());
            String correlationId = root.path("correlationId").asText("");
            Instant occurredAt = root.hasNonNull("occurredAt")
                    ? Instant.parse(root.path("occurredAt").asText())
                    : Instant.now();

            MDC.put("correlationId", correlationId);
            MDC.put("contratoId", aggregateId.toString());
            MDC.put("eventId", eventId.toString());

            log.info("auditoria.consumo.inicio evento={} aggregateId={} eventId={} correlationId={} partition={} offset={}",
                    eventType, aggregateId, eventId, correlationId, record.partition(), record.offset());

            auditoriaService.registrar(eventId, aggregateId, eventType, correlationId, record.value(), occurredAt);

            log.info("auditoria.consumo.concluido evento={} aggregateId={} eventId={} resultado=REGISTRADO_COM_SUCESSO",
                    eventType, aggregateId, eventId);

        } catch (Exception e) {
            log.error("auditoria.consumo.erro erro={} offset={}", e.getMessage(), record.offset(), e);
            throw new RuntimeException("Falha ao processar mensagem no auditoria-service", e);
        } finally {
            MDC.remove("correlationId");
            MDC.remove("contratoId");
            MDC.remove("eventId");
        }
    }
}
