package br.com.freela.reputacao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class ReputacaoKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(ReputacaoKafkaConsumer.class);

    private final ReputacaoService reputacaoService;
    private final MensagemProcessadaRepository mensagemProcessadaRepository;
    private final ObjectMapper objectMapper;

    public ReputacaoKafkaConsumer(
            ReputacaoService reputacaoService,
            MensagemProcessadaRepository mensagemProcessadaRepository,
            ObjectMapper objectMapper
    ) {
        this.reputacaoService = reputacaoService;
        this.mensagemProcessadaRepository = mensagemProcessadaRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.kafka.topics.contratos:contratos.v1.events}", groupId = "reputacao-group")
    @Transactional
    public void consumir(ConsumerRecord<String, String> record) {
        try {
            JsonNode root = objectMapper.readTree(record.value());

            UUID eventId = UUID.fromString(root.path("eventId").asText());
            String eventType = root.path("eventType").asText();
            UUID contratoId = UUID.fromString(root.path("contratoId").asText());
            String correlationId = root.path("correlationId").asText("");

            MDC.put("correlationId", correlationId);
            MDC.put("contratoId", contratoId.toString());
            MDC.put("eventId", eventId.toString());

            // A reputação só reage a eventos que impactam histórico ou reputação do freelancer
            if (!"ContratoConcluido".equalsIgnoreCase(eventType)) {
                log.debug("reputacao.evento.ignorado evento={} contratoId={}", eventType, contratoId);
                return;
            }

            UUID freelancerId = UUID.fromString(root.path("freelancerId").asText());
            BigDecimal valor = new BigDecimal(root.path("valor").asText("0"));

            log.info("reputacao.consumo.inicio evento={} contratoId={} freelancerId={} valor={} eventId={} correlationId={} partition={} offset={}",
                    eventType, contratoId, freelancerId, valor, eventId, correlationId, record.partition(), record.offset());

            // 5. Tratamento de mensagens duplicadas (Garantia de não duplicar incrementos)
            if (mensagemProcessadaRepository.existsById(eventId)) {
                log.warn("reputacao.consumo.duplicada.ignorada evento={} contratoId={} freelancerId={} eventId={} resultado=IGNORADO_SEM_INCREMENTO",
                        eventType, contratoId, freelancerId, eventId);
                return;
            }

            reputacaoService.registrarContratoConcluido(contratoId, freelancerId, valor);
            mensagemProcessadaRepository.save(new MensagemProcessada(eventId, eventType, "reputacao-service"));

            log.info("reputacao.consumo.concluido evento={} contratoId={} freelancerId={} eventId={} resultado=REPUTACAO_ATUALIZADA",
                    eventType, contratoId, freelancerId, eventId);

        } catch (Exception e) {
            log.error("reputacao.consumo.erro erro={} offset={}", e.getMessage(), record.offset(), e);
            throw new RuntimeException("Falha ao processar mensagem no reputacao-service", e);
        } finally {
            MDC.remove("correlationId");
            MDC.remove("contratoId");
            MDC.remove("eventId");
        }
    }
}
