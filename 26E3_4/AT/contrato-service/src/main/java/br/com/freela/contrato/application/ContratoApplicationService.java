package br.com.freela.contrato.application;

import br.com.freela.contrato.domain.model.Contrato;
import br.com.freela.contrato.domain.repository.ContratoRepository;
import br.com.freela.contrato.domain.shared.DomainEvent;
import br.com.freela.contrato.infrastructure.outbox.OutboxEventEntity;
import br.com.freela.contrato.infrastructure.outbox.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ContratoApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ContratoApplicationService.class);

    private final ContratoRepository repository;
    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public ContratoApplicationService(
            ContratoRepository repository,
            OutboxEventRepository outboxRepository,
            ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Contrato criar(CriarContratoCommand cmd) {
        return criar(cmd, resolveCorrelationId());
    }

    @Transactional
    public Contrato criar(CriarContratoCommand cmd, String correlationId) {
        log.info("contrato.criacao.inicio clienteId={} freelancerId={} titulo={} valor={} correlationId={}",
                cmd.clienteId(), cmd.freelancerId(), cmd.titulo(), cmd.valor(), correlationId);

        Contrato contrato = Contrato.criar(cmd.clienteId(), cmd.freelancerId(), cmd.titulo(), cmd.valor(), correlationId);
        log.info("contrato.dominio.criado contratoId={} status={} domainEvents={}",
                contrato.id(), contrato.status(), contrato.domainEvents().size());

        Contrato salvo = repository.salvar(contrato);
        salvarEventosNoOutbox(contrato, correlationId);

        log.info("contrato.criacao.sucesso contratoId={} clienteId={} freelancerId={} status={}",
                salvo.id(), salvo.clienteId(), salvo.freelancerId(), salvo.status());
        return salvo;
    }

    @Transactional
    public Contrato registrarEntrega(UUID id) {
        return registrarEntrega(id, resolveCorrelationId());
    }

    @Transactional
    public Contrato registrarEntrega(UUID id, String correlationId) {
        log.info("contrato.entrega.inicio contratoId={} correlationId={}", id, correlationId);
        Contrato contrato = buscar(id);
        contrato.registrarEntrega(correlationId);

        Contrato salvo = repository.salvar(contrato);
        salvarEventosNoOutbox(contrato, correlationId);

        log.info("contrato.entrega.sucesso contratoId={} status={}", salvo.id(), salvo.status());
        return salvo;
    }

    @Transactional
    public Contrato concluir(UUID id) {
        return concluir(id, resolveCorrelationId());
    }

    @Transactional
    public Contrato concluir(UUID id, String correlationId) {
        log.info("contrato.conclusao.inicio contratoId={} correlationId={}", id, correlationId);
        Contrato contrato = buscar(id);
        contrato.concluir(correlationId);

        Contrato salvo = repository.salvar(contrato);
        salvarEventosNoOutbox(contrato, correlationId);

        log.info("contrato.conclusao.sucesso contratoId={} status={}", salvo.id(), salvo.status());
        return salvo;
    }

    @Transactional
    public Contrato cancelar(UUID id) {
        return cancelar(id, resolveCorrelationId());
    }

    @Transactional
    public Contrato cancelar(UUID id, String correlationId) {
        log.info("contrato.cancelamento.inicio contratoId={} correlationId={}", id, correlationId);
        Contrato contrato = buscar(id);
        contrato.cancelar(correlationId);

        Contrato salvo = repository.salvar(contrato);
        salvarEventosNoOutbox(contrato, correlationId);

        log.info("contrato.cancelamento.sucesso contratoId={} status={}", salvo.id(), salvo.status());
        return salvo;
    }

    private void salvarEventosNoOutbox(Contrato contrato, String correlationId) {
        for (DomainEvent event : contrato.pullDomainEvents()) {
            try {
                String payloadJson = objectMapper.writeValueAsString(event);
                OutboxEventEntity outboxEvent = new OutboxEventEntity(
                        event.eventId(),
                        "Contrato",
                        event.contratoId(),
                        event.eventType(),
                        payloadJson,
                        correlationId
                );
                outboxRepository.save(outboxEvent);
                log.info("contrato.outbox.gravado contratoId={} eventId={} eventType={} correlationId={}",
                        contrato.id(), event.eventId(), event.eventType(), correlationId);
            } catch (Exception e) {
                log.error("contrato.outbox.erro ao serializar evento contratoId={} eventId={}",
                        contrato.id(), event.eventId(), e);
                throw new IllegalStateException("Falha ao salvar evento no outbox", e);
            }
        }
    }

    private String resolveCorrelationId() {
        String correlationId = MDC.get("correlationId");
        return (correlationId != null && !correlationId.isBlank()) ? correlationId : UUID.randomUUID().toString();
    }

    @Transactional(readOnly = true)
    public Contrato buscar(UUID id) {
        log.info("contrato.busca.inicio contratoId={}", id);
        var contrato = repository.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado: " + id));
        log.info("contrato.busca.sucesso contratoId={} status={}", id, contrato.status());
        return contrato;
    }

    @Transactional(readOnly = true)
    public List<Contrato> listar() {
        log.info("contrato.listagem.inicio");
        var contratos = repository.listar();
        log.info("contrato.listagem.sucesso quantidade={}", contratos.size());
        return contratos;
    }
}
