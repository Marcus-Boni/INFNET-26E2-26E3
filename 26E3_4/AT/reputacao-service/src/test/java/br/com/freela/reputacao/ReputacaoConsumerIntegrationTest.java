package br.com.freela.reputacao;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ReputacaoConsumerIntegrationTest {

    @Autowired
    private ReputacaoKafkaConsumer consumer;

    @Autowired
    private ReputacaoRepository reputacaoRepository;

    @Autowired
    private MensagemProcessadaRepository mensagemProcessadaRepository;

    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @BeforeEach
    void setUp() {
        reputacaoRepository.deleteAll();
        mensagemProcessadaRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve reagir ao ContratoConcluido e incrementar reputação do freelancer")
    void deveAtualizarReputacaoAoConcluirContrato() {
        UUID eventId = UUID.randomUUID();
        UUID contratoId = UUID.randomUUID();
        UUID freelancerId = UUID.randomUUID();

        String payload = String.format("""
                {
                    "eventId": "%s",
                    "eventType": "ContratoConcluido",
                    "contratoId": "%s",
                    "freelancerId": "%s",
                    "valor": "3500.00",
                    "correlationId": "corr-reput-001"
                }
                """, eventId, contratoId, freelancerId);

        consumer.consumir(new ConsumerRecord<>("contratos.v1.events", 0, 200L, contratoId.toString(), payload));

        var rep = reputacaoRepository.findById(freelancerId).orElseThrow();
        assertEquals(1, rep.getContratosConcluidos());
        assertEquals(new BigDecimal("3500.00"), rep.getValorTotal());
        assertTrue(mensagemProcessadaRepository.existsById(eventId));
    }

    @Test
    @DisplayName("Idempotência Crítica: Reprocessar a mesma mensagem NÃO deve duplicar nem incrementar novamente a reputação")
    void reprocessarMesmaMensagemNaoDeveIncrementarNovamente() {
        UUID eventId = UUID.randomUUID();
        UUID contratoId = UUID.randomUUID();
        UUID freelancerId = UUID.randomUUID();

        String payload = String.format("""
                {
                    "eventId": "%s",
                    "eventType": "ContratoConcluido",
                    "contratoId": "%s",
                    "freelancerId": "%s",
                    "valor": "2000.00",
                    "correlationId": "corr-reput-dup"
                }
                """, eventId, contratoId, freelancerId);

        ConsumerRecord<String, String> record = new ConsumerRecord<>("contratos.v1.events", 0, 201L, contratoId.toString(), payload);

        // 1º Processamento
        consumer.consumir(record);
        var rep1 = reputacaoRepository.findById(freelancerId).orElseThrow();
        assertEquals(1, rep1.getContratosConcluidos());
        assertEquals(new BigDecimal("2000.00"), rep1.getValorTotal());

        // 2º Processamento com a MESMA mensagem (mesmo eventId)
        consumer.consumir(record);
        var rep2 = reputacaoRepository.findById(freelancerId).orElseThrow();
        assertEquals(1, rep2.getContratosConcluidos(), "A contagem de contratos NÃO pode ser incrementada novamente!");
        assertEquals(new BigDecimal("2000.00"), rep2.getValorTotal(), "O valor total NÃO pode ser somado novamente!");
    }

    @Test
    @DisplayName("Deve ignorar eventos não relacionados à reputação")
    void deveIgnorarEventosNaoRelacionados() {
        UUID eventId = UUID.randomUUID();
        UUID contratoId = UUID.randomUUID();
        UUID freelancerId = UUID.randomUUID();

        String payload = String.format("""
                {
                    "eventId": "%s",
                    "eventType": "ContratoCriado",
                    "contratoId": "%s",
                    "freelancerId": "%s",
                    "valor": "1000.00",
                    "correlationId": "corr-reput-ignore"
                }
                """, eventId, contratoId, freelancerId);

        consumer.consumir(new ConsumerRecord<>("contratos.v1.events", 0, 202L, contratoId.toString(), payload));

        assertTrue(reputacaoRepository.findAll().isEmpty(), "Não deve alterar dados de reputação para ContratoCriado");
        assertFalse(mensagemProcessadaRepository.existsById(eventId));
    }

    @Test
    @DisplayName("Deve acumular múltiplos contratos distintos corretamente")
    void deveAcumularMultiplosContratosDistintos() {
        UUID freelancerId = UUID.randomUUID();

        // Contrato 1
        String payload1 = String.format("""
                {
                    "eventId": "%s",
                    "eventType": "ContratoConcluido",
                    "contratoId": "%s",
                    "freelancerId": "%s",
                    "valor": "3000.00",
                    "correlationId": "corr-reput-1"
                }
                """, UUID.randomUUID(), UUID.randomUUID(), freelancerId);
        consumer.consumir(new ConsumerRecord<>("contratos.v1.events", 0, 203L, "k1", payload1));

        // Contrato 2
        String payload2 = String.format("""
                {
                    "eventId": "%s",
                    "eventType": "ContratoConcluido",
                    "contratoId": "%s",
                    "freelancerId": "%s",
                    "valor": "2500.00",
                    "correlationId": "corr-reput-2"
                }
                """, UUID.randomUUID(), UUID.randomUUID(), freelancerId);
        consumer.consumir(new ConsumerRecord<>("contratos.v1.events", 0, 204L, "k2", payload2));

        var rep = reputacaoRepository.findById(freelancerId).orElseThrow();
        assertEquals(2, rep.getContratosConcluidos());
        assertEquals(new BigDecimal("5500.00"), rep.getValorTotal());
    }
}
