package br.com.freela.notificacao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class NotificacaoKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoKafkaConsumer.class);

    private final NotificacaoService notificacaoService;
    private final MensagemProcessadaRepository mensagemProcessadaRepository;
    private final ObjectMapper objectMapper;

    public NotificacaoKafkaConsumer(
            NotificacaoService notificacaoService,
            MensagemProcessadaRepository mensagemProcessadaRepository,
            ObjectMapper objectMapper
    ) {
        this.notificacaoService = notificacaoService;
        this.mensagemProcessadaRepository = mensagemProcessadaRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.kafka.topics.contratos:contratos.v1.events}", groupId = "notificacao-group")
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

            log.info("notificacao.consumo.inicio evento={} contratoId={} eventId={} correlationId={} partition={} offset={}",
                    eventType, contratoId, eventId, correlationId, record.partition(), record.offset());

            // 5. Tratamento de mensagens duplicadas (Idempotência)
            if (mensagemProcessadaRepository.existsById(eventId)) {
                log.warn("notificacao.consumo.duplicada.ignorada evento={} contratoId={} eventId={} resultado=IGNORADO_DUPLICADO",
                        eventType, contratoId, eventId);
                return;
            }

            UUID clienteId = root.hasNonNull("clienteId") ? UUID.fromString(root.path("clienteId").asText()) : null;
            UUID freelancerId = root.hasNonNull("freelancerId") ? UUID.fromString(root.path("freelancerId").asText()) : null;
            String titulo = root.path("titulo").asText("Sem título");
            String valor = root.hasNonNull("valor") ? root.path("valor").asText() : "";

            switch (eventType) {
                case "ContratoCriado" -> {
                    if (freelancerId != null) {
                        String msg = "Novo contrato recebido: '" + titulo + "' no valor de R$ " + valor;
                        notificacaoService.registrar(contratoId, freelancerId, "CONTRATO_CRIADO", msg);
                        log.info("notificacao.processada evento={} contratoId={} destinatarioId={} resultado=SUCESSO",
                                eventType, contratoId, freelancerId);
                    }
                    if (clienteId != null) {
                        String msg = "Contrato criado com sucesso: '" + titulo + "'";
                        notificacaoService.registrar(contratoId, clienteId, "CONTRATO_CRIADO", msg);
                        log.info("notificacao.processada evento={} contratoId={} destinatarioId={} resultado=SUCESSO",
                                eventType, contratoId, clienteId);
                    }
                }
                case "EntregaRegistrada" -> {
                    if (clienteId != null) {
                        String msg = "O freelancer realizou uma entrega para o contrato: '" + titulo + "'";
                        notificacaoService.registrar(contratoId, clienteId, "ENTREGA_REGISTRADA", msg);
                        log.info("notificacao.processada evento={} contratoId={} destinatarioId={} resultado=SUCESSO",
                                eventType, contratoId, clienteId);
                    }
                }
                case "ContratoConcluido" -> {
                    if (freelancerId != null) {
                        String msg = "O contrato '" + titulo + "' foi concluído e o pagamento foi liberado.";
                        notificacaoService.registrar(contratoId, freelancerId, "CONTRATO_CONCLUIDO", msg);
                        log.info("notificacao.processada evento={} contratoId={} destinatarioId={} resultado=SUCESSO",
                                eventType, contratoId, freelancerId);
                    }
                }
                case "ContratoCancelado" -> {
                    if (freelancerId != null) {
                        notificacaoService.registrar(contratoId, freelancerId, "CONTRATO_CANCELADO", "O contrato '" + titulo + "' foi cancelado.");
                    }
                    if (clienteId != null) {
                        notificacaoService.registrar(contratoId, clienteId, "CONTRATO_CANCELADO", "O contrato '" + titulo + "' foi cancelado.");
                    }
                }
                default -> log.debug("notificacao.evento.ignorado evento={}", eventType);
            }

            mensagemProcessadaRepository.save(new MensagemProcessada(eventId, eventType, "notificacao-service"));
            log.info("notificacao.consumo.concluido evento={} contratoId={} eventId={} resultado=PROCESSADO_COM_SUCESSO",
                    eventType, contratoId, eventId);

        } catch (Exception e) {
            log.error("notificacao.consumo.erro erro={} offset={}", e.getMessage(), record.offset(), e);
            throw new RuntimeException("Falha ao processar mensagem no notificacao-service", e);
        } finally {
            MDC.remove("correlationId");
            MDC.remove("contratoId");
            MDC.remove("eventId");
        }
    }
}
