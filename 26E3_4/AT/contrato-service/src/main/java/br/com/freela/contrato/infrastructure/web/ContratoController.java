package br.com.freela.contrato.infrastructure.web;

import br.com.freela.contrato.application.ContratoApplicationService;
import br.com.freela.contrato.application.CriarContratoCommand;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/contratos")
public class ContratoController {

    private static final Logger log = LoggerFactory.getLogger(ContratoController.class);
    private final ContratoApplicationService service;

    public ContratoController(ContratoApplicationService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContratoResponse criar(
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @Valid @RequestBody CriarContratoRequest request
    ) {
        String corrId = resolveCorrelationId(correlationId);
        try {
            MDC.put("correlationId", corrId);
            log.info("http.contrato.criar correlationId={} clienteId={} freelancerId={} titulo={} valor={}",
                    corrId, request.clienteId(), request.freelancerId(), request.titulo(), request.valor());

            var c = service.criar(new CriarContratoCommand(request.clienteId(), request.freelancerId(), request.titulo(), request.valor()), corrId);
            MDC.put("contratoId", c.id().toString());
            log.info("http.contrato.criar.response correlationId={} contratoId={} status={}", corrId, c.id(), c.status());
            return ContratoResponse.from(c);
        } finally {
            MDC.clear();
        }
    }

    @PutMapping("/{id}/entrega")
    public ContratoResponse registrarEntrega(
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable UUID id
    ) {
        String corrId = resolveCorrelationId(correlationId);
        try {
            MDC.put("correlationId", corrId);
            MDC.put("contratoId", id.toString());
            log.info("http.contrato.entrega correlationId={} contratoId={}", corrId, id);

            var c = service.registrarEntrega(id, corrId);
            log.info("http.contrato.entrega.response correlationId={} contratoId={} status={}", corrId, c.id(), c.status());
            return ContratoResponse.from(c);
        } finally {
            MDC.clear();
        }
    }

    @PutMapping("/{id}/concluir")
    public ContratoResponse concluir(
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable UUID id
    ) {
        String corrId = resolveCorrelationId(correlationId);
        try {
            MDC.put("correlationId", corrId);
            MDC.put("contratoId", id.toString());
            log.info("http.contrato.concluir correlationId={} contratoId={}", corrId, id);

            var c = service.concluir(id, corrId);
            log.info("http.contrato.concluir.response correlationId={} contratoId={} status={}", corrId, c.id(), c.status());
            return ContratoResponse.from(c);
        } finally {
            MDC.clear();
        }
    }

    @PutMapping("/{id}/cancelar")
    public ContratoResponse cancelar(
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable UUID id
    ) {
        String corrId = resolveCorrelationId(correlationId);
        try {
            MDC.put("correlationId", corrId);
            MDC.put("contratoId", id.toString());
            log.info("http.contrato.cancelar correlationId={} contratoId={}", corrId, id);

            var c = service.cancelar(id, corrId);
            log.info("http.contrato.cancelar.response correlationId={} contratoId={} status={}", corrId, c.id(), c.status());
            return ContratoResponse.from(c);
        } finally {
            MDC.clear();
        }
    }

    @GetMapping("/{id}")
    public ContratoResponse buscar(
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable UUID id
    ) {
        String corrId = resolveCorrelationId(correlationId);
        try {
            MDC.put("correlationId", corrId);
            MDC.put("contratoId", id.toString());
            log.info("http.contrato.buscar correlationId={} contratoId={}", corrId, id);
            return ContratoResponse.from(service.buscar(id));
        } finally {
            MDC.clear();
        }
    }

    @GetMapping
    public List<ContratoResponse> listar(
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId
    ) {
        String corrId = resolveCorrelationId(correlationId);
        try {
            MDC.put("correlationId", corrId);
            log.info("http.contrato.listar correlationId={}", corrId);
            return service.listar().stream().map(ContratoResponse::from).toList();
        } finally {
            MDC.clear();
        }
    }

    private String resolveCorrelationId(String correlationId) {
        return (correlationId != null && !correlationId.isBlank()) ? correlationId : UUID.randomUUID().toString();
    }
}
