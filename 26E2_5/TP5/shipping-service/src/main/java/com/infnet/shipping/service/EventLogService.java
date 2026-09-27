package com.infnet.shipping.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infnet.shipping.domain.model.EventLog;
import com.infnet.shipping.domain.repository.EventLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventLogService {

    private static final Logger log = LoggerFactory.getLogger(EventLogService.class);

    private final EventLogRepository eventLogRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public EventLogService(EventLogRepository eventLogRepository, ObjectMapper objectMapper) {
        this.eventLogRepository = eventLogRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public EventLog logPublishedEvent(String eventId, String correlationId, String eventType,
                                      String exchange, String routingKey, Object payload,
                                      String status, String details) {
        String payloadJson = serializePayload(payload);
        EventLog eventLog = EventLog.builder()
                .eventId(eventId != null ? eventId : java.util.UUID.randomUUID().toString())
                .correlationId(correlationId)
                .eventType(eventType)
                .exchange(exchange)
                .routingKey(routingKey)
                .direction("PUBLISHED")
                .status(status != null ? status : "SUCCESS")
                .payload(payloadJson)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();

        log.info("[EDA-SHIPPING] Evento publicado: tipo={}, exchange={}, routingKey={}, correlationId={}",
                eventType, exchange, routingKey, correlationId);
        return eventLogRepository.save(eventLog);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public EventLog logReceivedEvent(String eventId, String correlationId, String eventType,
                                     String exchange, String routingKey, Object payload,
                                     String status, String details) {
        String payloadJson = serializePayload(payload);
        EventLog eventLog = EventLog.builder()
                .eventId(eventId != null ? eventId : java.util.UUID.randomUUID().toString())
                .correlationId(correlationId)
                .eventType(eventType)
                .exchange(exchange)
                .routingKey(routingKey)
                .direction("RECEIVED")
                .status(status != null ? status : "SUCCESS")
                .payload(payloadJson)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();

        log.info("[EDA-SHIPPING] Evento recebido: tipo={}, exchange={}, routingKey={}, correlationId={}",
                eventType, exchange, routingKey, correlationId);
        return eventLogRepository.save(eventLog);
    }

    @Transactional(readOnly = true)
    public List<EventLog> findAllEvents() {
        return eventLogRepository.findAllByOrderByTimestampDesc();
    }

    @Transactional(readOnly = true)
    public List<EventLog> findByCorrelationId(String correlationId) {
        return eventLogRepository.findByCorrelationIdOrderByTimestampAsc(correlationId);
    }

    private String serializePayload(Object payload) {
        if (payload == null) return null;
        if (payload instanceof String) return (String) payload;
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            return payload.toString();
        }
    }
}
