package com.infnet.tp5.controller;

import com.infnet.tp5.domain.model.EventLog;
import com.infnet.tp5.infrastructure.config.RabbitMqConfig;
import com.infnet.tp5.service.EventLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventMonitoringController {

    private static final Logger log = LoggerFactory.getLogger(EventMonitoringController.class);

    private final EventLogService eventLogService;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public EventMonitoringController(EventLogService eventLogService, RabbitTemplate rabbitTemplate) {
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
    public ResponseEntity<Map<String, Object>> getRabbitMqTopology() {
        Map<String, Object> topology = new LinkedHashMap<>();
        topology.put("broker", "RabbitMQ (AMQP 0-9-1)");
        topology.put("status", "CONNECTED");
        
        List<Map<String, String>> exchanges = List.of(
                Map.of("name", RabbitMqConfig.EXCHANGE_ORDER, "type", "topic", "durability", "durable"),
                Map.of("name", RabbitMqConfig.EXCHANGE_SHIPPING, "type", "topic", "durability", "durable"),
                Map.of("name", RabbitMqConfig.EXCHANGE_DLX, "type", "topic", "durability", "durable")
        );
        topology.put("exchanges", exchanges);

        List<Map<String, String>> queues = List.of(
                Map.of("name", RabbitMqConfig.QUEUE_ORDER_SHIPMENT_CREATED, "exchange", RabbitMqConfig.EXCHANGE_SHIPPING, "routingKey", RabbitMqConfig.ROUTING_KEY_SHIPPING_CREATED, "dlq", RabbitMqConfig.QUEUE_ORDER_DLQ),
                Map.of("name", RabbitMqConfig.QUEUE_ORDER_SHIPMENT_STATUS_UPDATED, "exchange", RabbitMqConfig.EXCHANGE_SHIPPING, "routingKey", RabbitMqConfig.ROUTING_KEY_SHIPPING_STATUS_UPDATED, "dlq", RabbitMqConfig.QUEUE_ORDER_DLQ),
                Map.of("name", RabbitMqConfig.QUEUE_SHIPPING_ORDER_CREATED, "exchange", RabbitMqConfig.EXCHANGE_ORDER, "routingKey", RabbitMqConfig.ROUTING_KEY_ORDER_CREATED, "dlq", RabbitMqConfig.QUEUE_SHIPPING_DLQ),
                Map.of("name", RabbitMqConfig.QUEUE_SHIPPING_ORDER_DISPATCHED, "exchange", RabbitMqConfig.EXCHANGE_ORDER, "routingKey", RabbitMqConfig.ROUTING_KEY_ORDER_DISPATCHED, "dlq", RabbitMqConfig.QUEUE_SHIPPING_DLQ),
                Map.of("name", RabbitMqConfig.QUEUE_SHIPPING_ORDER_CANCELLED, "exchange", RabbitMqConfig.EXCHANGE_ORDER, "routingKey", RabbitMqConfig.ROUTING_KEY_ORDER_CANCELLED, "dlq", RabbitMqConfig.QUEUE_SHIPPING_DLQ),
                Map.of("name", RabbitMqConfig.QUEUE_ORDER_DLQ, "exchange", RabbitMqConfig.EXCHANGE_DLX, "routingKey", "order.dlq.#", "dlq", "N/A"),
                Map.of("name", RabbitMqConfig.QUEUE_SHIPPING_DLQ, "exchange", RabbitMqConfig.EXCHANGE_DLX, "routingKey", "shipping.dlq.#", "dlq", "N/A")
        );
        topology.put("queues", queues);

        return ResponseEntity.ok(topology);
    }

    @PostMapping("/simulate-dlq")
    public ResponseEntity<Map<String, Object>> simulateDeadLetterQueue(
            @RequestParam(defaultValue = "order") String target,
            @RequestParam(required = false) String reason) {
        
        String eventId = UUID.randomUUID().toString();
        String correlationId = "corr-dlq-test-" + UUID.randomUUID().toString().substring(0, 8);
        String dlqReason = reason != null ? reason : "Simulação de mensagem corrompida / falha de deserialização intencional";

        Map<String, Object> poisonMessage = new HashMap<>();
        poisonMessage.put("eventId", eventId);
        poisonMessage.put("correlationId", correlationId);
        poisonMessage.put("eventType", "CORRUPTED_POISON_PILL");
        poisonMessage.put("timestamp", LocalDateTime.now().toString());
        poisonMessage.put("errorSimulation", dlqReason);
        poisonMessage.put("invalidField", -99999);

        try {
            // Envia diretamente para o DLX com a routing key configurada
            String dlqRoutingKey = "order".equalsIgnoreCase(target) ? RabbitMqConfig.ROUTING_KEY_ORDER_DLQ : RabbitMqConfig.ROUTING_KEY_SHIPPING_DLQ;
            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.EXCHANGE_DLX,
                    dlqRoutingKey,
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
                    dlqRoutingKey,
                    poisonMessage,
                    "DLQ",
                    "Mensagem venenosa injetada diretamente na Dead Letter Queue (" + dlqRoutingKey + ") para testes de resiliência"
            );

            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "Mensagem enviada com sucesso para a Dead Letter Queue!",
                    "targetQueue", dlqRoutingKey,
                    "correlationId", correlationId,
                    "eventId", eventId
            ));
        } catch (Exception ex) {
            log.warn("Erro ao simular envio para DLQ no RabbitMQ: {}", ex.getMessage());
            eventLogService.logPublishedEvent(
                    eventId,
                    correlationId,
                    "SIMULATED_POISON_PILL_DLQ",
                    RabbitMqConfig.EXCHANGE_DLX,
                    "dlq",
                    poisonMessage,
                    "FAILED",
                    "Broker offline no momento da simulação: " + ex.getMessage()
            );

            return ResponseEntity.ok(Map.of(
                    "status", "SIMULATED_LOCAL",
                    "message", "Simulação registrada localmente no log de eventos (RabbitMQ broker não conectado).",
                    "correlationId", correlationId,
                    "eventId", eventId
            ));
        }
    }
}

