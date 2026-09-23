package br.com.freela.contrato.infrastructure.outbox;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxEventRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;

    public OutboxPublisher(
            OutboxEventRepository outboxRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${app.kafka.topics.contratos:contratos.v1.events}") String topic
    ) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Scheduled(fixedDelayString = "${app.outbox.poll-interval-ms:500}")
    public void publishPendingEvents() {
        List<OutboxEventEntity> pendingEvents = outboxRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
        if (pendingEvents.isEmpty()) {
            return;
        }

        for (OutboxEventEntity event : pendingEvents) {
            publishSingleEvent(event);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishSingleEvent(OutboxEventEntity event) {
        String correlationId = event.getCorrelationId() != null ? event.getCorrelationId() : "";
        try {
            MDC.put("correlationId", correlationId);
            MDC.put("contratoId", event.getAggregateId().toString());
            MDC.put("eventId", event.getId().toString());

            log.info("outbox.publicacao.inicio eventId={} aggregateId={} eventType={} topic={} key={}",
                    event.getId(), event.getAggregateId(), event.getEventType(), topic, event.getAggregateId());

            ProducerRecord<String, String> record = new ProducerRecord<>(
                    topic,
                    event.getAggregateId().toString(), // Chave de particionamento garante FIFO por contrato
                    event.getPayload()
            );

            if (event.getCorrelationId() != null) {
                record.headers().add(new RecordHeader("X-Correlation-Id", event.getCorrelationId().getBytes(StandardCharsets.UTF_8)));
                record.headers().add(new RecordHeader("correlationId", event.getCorrelationId().getBytes(StandardCharsets.UTF_8)));
            }
            record.headers().add(new RecordHeader("eventId", event.getId().toString().getBytes(StandardCharsets.UTF_8)));
            record.headers().add(new RecordHeader("eventType", event.getEventType().getBytes(StandardCharsets.UTF_8)));

            // Envio síncrono com timeout para garantir confirmação de entrega do broker
            var sendResult = kafkaTemplate.send(record).get(5, TimeUnit.SECONDS);

            event.markAsPublished();
            outboxRepository.save(event);

            log.info("outbox.publicacao.sucesso eventId={} aggregateId={} eventType={} partition={} offset={}",
                    event.getId(), event.getAggregateId(), event.getEventType(),
                    sendResult.getRecordMetadata().partition(), sendResult.getRecordMetadata().offset());

        } catch (Exception e) {
            log.error("outbox.publicacao.falha eventId={} aggregateId={} eventType={} erro={}",
                    event.getId(), event.getAggregateId(), event.getEventType(), e.getMessage(), e);
            event.markAsFailed(e.getMessage(), 5);
            outboxRepository.save(event);
        } finally {
            MDC.remove("correlationId");
            MDC.remove("contratoId");
            MDC.remove("eventId");
        }
    }
}
