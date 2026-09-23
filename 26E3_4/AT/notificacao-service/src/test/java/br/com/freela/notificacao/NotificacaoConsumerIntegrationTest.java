package br.com.freela.notificacao;

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
class NotificacaoConsumerIntegrationTest {

    @Autowired
    private NotificacaoKafkaConsumer consumer;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private MensagemProcessadaRepository mensagemProcessadaRepository;

    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @BeforeEach
    void setUp() {
        notificacaoRepository.deleteAll();
        mensagemProcessadaRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve consumir ContratoCriado e gerar notificações para cliente e freelancer")
    void deveConsumirContratoCriado() {
        UUID eventId = UUID.randomUUID();
        UUID contratoId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        UUID freelancerId = UUID.randomUUID();

        String payload = String.format("""
                {
                    "eventId": "%s",
                    "eventType": "ContratoCriado",
                    "contratoId": "%s",
                    "clienteId": "%s",
                    "freelancerId": "%s",
                    "titulo": "Desenvolvimento Frontend",
                    "valor": "3000.00",
                    "correlationId": "corr-notif-001"
                }
                """, eventId, contratoId, clienteId, freelancerId);

        ConsumerRecord<String, String> record = new ConsumerRecord<>("contratos.v1.events", 0, 100L, contratoId.toString(), payload);
        consumer.consumir(record);

        var notificacoes = notificacaoRepository.findAll();
        assertEquals(2, notificacoes.size(), "Devem ser criadas 2 notificações (cliente e freelancer)");
        assertTrue(mensagemProcessadaRepository.existsById(eventId), "O eventId deve estar registrado em mensagens_processadas");
    }

    @Test
    @DisplayName("Idempotência: Reprocessar o mesmo evento não deve duplicar notificações")
    void reprocessarMesmoEventoNaoDeveDuplicar() {
        UUID eventId = UUID.randomUUID();
        UUID contratoId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        UUID freelancerId = UUID.randomUUID();

        String payload = String.format("""
                {
                    "eventId": "%s",
                    "eventType": "ContratoCriado",
                    "contratoId": "%s",
                    "clienteId": "%s",
                    "freelancerId": "%s",
                    "titulo": "Desenvolvimento Mobile",
                    "valor": "4000.00",
                    "correlationId": "corr-notif-dup"
                }
                """, eventId, contratoId, clienteId, freelancerId);

        ConsumerRecord<String, String> record = new ConsumerRecord<>("contratos.v1.events", 0, 101L, contratoId.toString(), payload);

        // 1ª execução
        consumer.consumir(record);
        assertEquals(2, notificacaoRepository.findAll().size());

        // 2ª execução com a MESMA mensagem (mesmo eventId)
        consumer.consumir(record);
        assertEquals(2, notificacaoRepository.findAll().size(), "Não deve duplicar notificações em caso de reentrega da mesma mensagem");
    }

    @Test
    @DisplayName("Deve gerar notificação na entrega e na conclusão")
    void deveGerarNotificacoesEntregaEConclusao() {
        UUID contratoId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        UUID freelancerId = UUID.randomUUID();

        // Entrega
        UUID eventEntregaId = UUID.randomUUID();
        String payloadEntrega = String.format("""
                {
                    "eventId": "%s",
                    "eventType": "EntregaRegistrada",
                    "contratoId": "%s",
                    "clienteId": "%s",
                    "freelancerId": "%s",
                    "titulo": "API Backend",
                    "correlationId": "corr-entrega"
                }
                """, eventEntregaId, contratoId, clienteId, freelancerId);
        consumer.consumir(new ConsumerRecord<>("contratos.v1.events", 0, 102L, contratoId.toString(), payloadEntrega));

        // Conclusão
        UUID eventConclusaoId = UUID.randomUUID();
        String payloadConclusao = String.format("""
                {
                    "eventId": "%s",
                    "eventType": "ContratoConcluido",
                    "contratoId": "%s",
                    "clienteId": "%s",
                    "freelancerId": "%s",
                    "titulo": "API Backend",
                    "valor": "5000.00",
                    "correlationId": "corr-conclusao"
                }
                """, eventConclusaoId, contratoId, clienteId, freelancerId);
        consumer.consumir(new ConsumerRecord<>("contratos.v1.events", 0, 103L, contratoId.toString(), payloadConclusao));

        var notificacoes = notificacaoRepository.findAll();
        assertEquals(2, notificacoes.size());
        assertTrue(notificacoes.stream().anyMatch(n -> "ENTREGA_REGISTRADA".equals(n.getTipo())));
        assertTrue(notificacoes.stream().anyMatch(n -> "CONTRATO_CONCLUIDO".equals(n.getTipo())));
    }
}
