package br.com.freela.auditoria;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AuditoriaConsumerIntegrationTest {

    @Autowired
    private AuditoriaKafkaConsumer consumer;

    @Autowired
    private EventoAuditoriaRepository repository;

    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("Deve auditar todos os eventos do ciclo de vida preservando ordem e correlationId")
    void deveAuditarEventosDoCicloDeVida() {
        UUID contratoId = UUID.randomUUID();
        String correlationId = "corr-audit-flow-001";

        // 1. ContratoCriado
        UUID event1Id = UUID.randomUUID();
        String payload1 = String.format("""
                {
                    "eventId": "%s",
                    "eventType": "ContratoCriado",
                    "contratoId": "%s",
                    "clienteId": "%s",
                    "freelancerId": "%s",
                    "titulo": "Auditoria de Segurança",
                    "valor": "6000.00",
                    "correlationId": "%s",
                    "occurredAt": "2026-09-22T12:00:00Z"
                }
                """, event1Id, contratoId, UUID.randomUUID(), UUID.randomUUID(), correlationId);
        consumer.consumir(new ConsumerRecord<>("contratos.v1.events", 0, 301L, contratoId.toString(), payload1));

        // 2. EntregaRegistrada
        UUID event2Id = UUID.randomUUID();
        String payload2 = String.format("""
                {
                    "eventId": "%s",
                    "eventType": "EntregaRegistrada",
                    "contratoId": "%s",
                    "clienteId": "%s",
                    "freelancerId": "%s",
                    "titulo": "Auditoria de Segurança",
                    "correlationId": "%s",
                    "occurredAt": "2026-09-22T13:00:00Z"
                }
                """, event2Id, contratoId, UUID.randomUUID(), UUID.randomUUID(), correlationId);
        consumer.consumir(new ConsumerRecord<>("contratos.v1.events", 0, 302L, contratoId.toString(), payload2));

        // 3. ContratoConcluido
        UUID event3Id = UUID.randomUUID();
        String payload3 = String.format("""
                {
                    "eventId": "%s",
                    "eventType": "ContratoConcluido",
                    "contratoId": "%s",
                    "clienteId": "%s",
                    "freelancerId": "%s",
                    "titulo": "Auditoria de Segurança",
                    "valor": "6000.00",
                    "correlationId": "%s",
                    "occurredAt": "2026-09-22T14:00:00Z"
                }
                """, event3Id, contratoId, UUID.randomUUID(), UUID.randomUUID(), correlationId);
        consumer.consumir(new ConsumerRecord<>("contratos.v1.events", 0, 303L, contratoId.toString(), payload3));

        // Consultar eventos do contrato
        var eventos = repository.findByAggregateIdOrderByRecebidoEmAsc(contratoId);
        assertEquals(3, eventos.size());
        assertEquals("ContratoCriado", eventos.get(0).getEventType());
        assertEquals("EntregaRegistrada", eventos.get(1).getEventType());
        assertEquals("ContratoConcluido", eventos.get(2).getEventType());
        assertEquals(correlationId, eventos.get(0).getCorrelationId());
        assertTrue(eventos.get(0).getPayload().contains("Auditoria de Segurança"));

        // Consultar por correlationId
        var eventosPorCorr = repository.findByCorrelationIdOrderByRecebidoEmAsc(correlationId);
        assertEquals(3, eventosPorCorr.size());
    }

    @Test
    @DisplayName("Idempotência: Reprocessar o mesmo evento não deve duplicar o registro de auditoria")
    void reprocessarMesmoEventoNaoDeveDuplicarAuditoria() {
        UUID eventId = UUID.randomUUID();
        UUID contratoId = UUID.randomUUID();

        String payload = String.format("""
                {
                    "eventId": "%s",
                    "eventType": "ContratoCriado",
                    "contratoId": "%s",
                    "correlationId": "corr-audit-dup",
                    "occurredAt": "2026-09-22T15:00:00Z"
                }
                """, eventId, contratoId);

        ConsumerRecord<String, String> record = new ConsumerRecord<>("contratos.v1.events", 0, 304L, contratoId.toString(), payload);

        // 1ª vez
        consumer.consumir(record);
        assertEquals(1, repository.findAll().size());

        // 2ª vez com o mesmo eventId
        consumer.consumir(record);
        assertEquals(1, repository.findAll().size(), "Não deve duplicar registros de auditoria com o mesmo eventId");
    }
}
