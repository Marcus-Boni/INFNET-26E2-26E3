package com.infnet.shipping.controller;

import com.infnet.shipping.domain.model.EventLog;
import com.infnet.shipping.infrastructure.config.RabbitMqConfig;
import com.infnet.shipping.service.EventLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/shipping/events")
public class ShippingEventMonitoringController {

    private static final Logger log = LoggerFactory.getLogger(ShippingEventMonitoringController.class);

    private final EventLogService eventLogService;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public ShippingEventMonitoringController(EventLogService eventLogService, RabbitTemplate rabbitTemplate) {
        this.eventLogService = eventLogService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @GetMapping
    public ResponseEntity<List<EventLog>> getAllEvents() {
        return ResponseEntity.ok(eventLogService.findAllEvents());
    }

    @GetMapping("/correlation/{correlationId}")
    public ResponseEntity<List<EventLog>> getEventsByCorrelation(@PathVariable String correlationId) {
        return ResponseEntity.ok(eventLogService.findByCorrelationId(correlationId));
    }

    @GetMapping("/topology")
    public ResponseEntity<Map<String, Object>> getTopology() {
        Map<String, Object> topology = new LinkedHashMap<>();
        topology.put("service", "shipping-service");
        topology.put("broker", "RabbitMQ (AMQP 0-9-1)");
        topology.put("status", "CONNECTED");
        
        List<Map<String, String>> queues = List.of(
                Map.of("queue", RabbitMqConfig.QUEUE_SHIPPING_ORDER_CREATED, "exchange", RabbitMqConfig.EXCHANGE_ORDER, "routingKey", RabbitMqConfig.ROUTING_KEY_ORDER_CREATED, "dlq", RabbitMqConfig.QUEUE_SHIPPING_DLQ),
                Map.of("queue", RabbitMqConfig.QUEUE_SHIPPING_ORDER_DISPATCHED, "exchange", RabbitMqConfig.EXCHANGE_ORDER, "routingKey", RabbitMqConfig.ROUTING_KEY_ORDER_DISPATCHED, "dlq", RabbitMqConfig.QUEUE_SHIPPING_DLQ),
                Map.of("queue", RabbitMqConfig.QUEUE_SHIPPING_ORDER_CANCELLED, "exchange", RabbitMqConfig.EXCHANGE_ORDER, "routingKey", RabbitMqConfig.ROUTING_KEY_ORDER_CANCELLED, "dlq", RabbitMqConfig.QUEUE_SHIPPING_DLQ),
                Map.of("queue", RabbitMqConfig.QUEUE_SHIPPING_DLQ, "exchange", RabbitMqConfig.EXCHANGE_DLX, "routingKey", "shipping.dlq.#", "dlq", "N/A")
        );
        topology.put("queues", queues);
        return ResponseEntity.ok(topology);
    }

    @PostMapping("/simulate-dlq")
    public ResponseEntity<Map<String, Object>> simulateDlq(@RequestParam(required = false) String reason) {
        String eventId = UUID.randomUUID().toString();
        String correlationId = "corr-ship-dlq-" + UUID.randomUUID().toString().substring(0, 8);
        String dlqReason = reason != null ? reason : "Simulação de falha no processamento de frete (DLQ)";

        Map<String, Object> poisonMessage = new HashMap<>();
        poisonMessage.put("eventId", eventId);
        poisonMessage.put("correlationId", correlationId);
        poisonMessage.put("eventType", "CORRUPTED_SHIPPING_MESSAGE");
        poisonMessage.put("timestamp", LocalDateTime.now().toString());
        poisonMessage.put("errorSimulation", dlqReason);

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.EXCHANGE_DLX,
                    RabbitMqConfig.ROUTING_KEY_SHIPPING_DLQ,
                    poisonMessage,
                    m -> {
                        m.getMessageProperties().setCorrelationId(correlationId);
                        m.getMessageProperties().setMessageId(eventId);
                        m.getMessageProperties().setHeader("x-death-reason", dlqReason);
                        return m;
                    }
            );

            eventLogService.logPublishedEvent(
                    eventId,
                    correlationId,
                    "SIMULATED_POISON_PILL_DLQ",
                    RabbitMqConfig.EXCHANGE_DLX,
                    RabbitMqConfig.ROUTING_KEY_SHIPPING_DLQ,
                    poisonMessage,
                    "DLQ",
                    "Mensagem venenosa injetada diretamente na Dead Letter Queue de Logística"
            );

            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "Mensagem enviada com sucesso para a Dead Letter Queue de Logística!",
                    "targetQueue", RabbitMqConfig.QUEUE_SHIPPING_DLQ,
                    "correlationId", correlationId,
                    "eventId", eventId
            ));
        } catch (Exception ex) {
            log.warn("Erro ao simular DLQ no shipping service: {}", ex.getMessage());
            eventLogService.logPublishedEvent(
                    eventId,
                    correlationId,
                    "SIMULATED_POISON_PILL_DLQ",
                    RabbitMqConfig.EXCHANGE_DLX,
                    "shipping.dlq",
                    poisonMessage,
                    "FAILED",
                    "Broker offline no momento da simulação: " + ex.getMessage()
            );

            return ResponseEntity.ok(Map.of(
                    "status", "SIMULATED_LOCAL",
                    "message", "Simulação registrada localmente no log de eventos da logística.",
                    "correlationId", correlationId,
                    "eventId", eventId
            ));
        }
    }
}
