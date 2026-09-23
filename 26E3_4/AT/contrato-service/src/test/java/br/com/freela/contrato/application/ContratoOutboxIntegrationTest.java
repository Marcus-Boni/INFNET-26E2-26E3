package br.com.freela.contrato.application;

import br.com.freela.contrato.domain.model.StatusContrato;
import br.com.freela.contrato.infrastructure.outbox.OutboxEventEntity;
import br.com.freela.contrato.infrastructure.outbox.OutboxEventRepository;
import br.com.freela.contrato.infrastructure.outbox.OutboxPublisher;
import br.com.freela.contrato.infrastructure.outbox.OutboxStatus;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class ContratoOutboxIntegrationTest {

    @Autowired
    private ContratoApplicationService service;

    @Autowired
    private OutboxEventRepository outboxRepository;

    @Autowired
    private OutboxPublisher outboxPublisher;

    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    @DisplayName("Deve persistir atomicamente Contrato e eventos no Outbox durante o ciclo de vida")
    void devePersistirContratoEOutboxAtomicamente() {
        UUID clienteId = UUID.randomUUID();
        UUID freelancerId = UUID.randomUUID();
        String correlationId = "corr-test-outbox-" + UUID.randomUUID();

        // 1. Criar Contrato
        var contrato = service.criar(new CriarContratoCommand(clienteId, freelancerId, "Sistema de Pagamentos", new BigDecimal("4500.00")), correlationId);
        assertNotNull(contrato.id());
        assertEquals(StatusContrato.ATIVO, contrato.status());

        // 2. Registrar Entrega
        service.registrarEntrega(contrato.id(), correlationId);

        // 3. Concluir Contrato
        service.concluir(contrato.id(), correlationId);

        // Verificar tabela outbox_events
        List<OutboxEventEntity> pendingEvents = outboxRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
        assertTrue(pendingEvents.size() >= 3, "Devem existir ao menos 3 eventos gerados no outbox");

        // Validar tipos e ordenação no outbox
        var eventosContrato = pendingEvents.stream()
                .filter(e -> e.getAggregateId().equals(contrato.id()))
                .toList();
        assertEquals(3, eventosContrato.size());
        assertEquals("ContratoCriado", eventosContrato.get(0).getEventType());
        assertEquals("EntregaRegistrada", eventosContrato.get(1).getEventType());
        assertEquals("ContratoConcluido", eventosContrato.get(2).getEventType());
        assertEquals(correlationId, eventosContrato.get(0).getCorrelationId());

        // Mock Kafka send
        RecordMetadata metadata = new RecordMetadata(new TopicPartition("contratos.v1.events", 0), 0, 0, 0, 0, 0);
        SendResult<String, String> sendResult = new SendResult<>(new ProducerRecord<>("contratos.v1.events", contrato.id().toString(), "{}"), metadata);
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(CompletableFuture.completedFuture(sendResult));

        // 4. Executar Outbox Publisher
        outboxPublisher.publishPendingEvents();

        // Verificar que eventos foram marcados como PUBLISHED
        var publishedContrato = outboxRepository.findAll().stream()
                .filter(e -> e.getAggregateId().equals(contrato.id()))
                .toList();
        assertTrue(publishedContrato.stream().allMatch(e -> e.getStatus() == OutboxStatus.PUBLISHED));
        assertTrue(publishedContrato.stream().allMatch(e -> e.getPublishedAt() != null));
    }
}
