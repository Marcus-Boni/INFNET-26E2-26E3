package br.com.freela.contrato.domain.model;

import br.com.freela.contrato.domain.event.ContratoConcluido;
import br.com.freela.contrato.domain.event.ContratoCriado;
import br.com.freela.contrato.domain.event.EntregaRegistrada;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ContratoDomainTest {

    @Test
    @DisplayName("Deve criar contrato ativo e registrar evento ContratoCriado")
    void deveCriarContratoAtivo() {
        UUID clienteId = UUID.randomUUID();
        UUID freelancerId = UUID.randomUUID();

        Contrato contrato = Contrato.criar(clienteId, freelancerId, "Desenvolvimento Web", new BigDecimal("5000.00"), "corr-123");

        assertNotNull(contrato.id());
        assertEquals(StatusContrato.ATIVO, contrato.status());
        assertEquals(1, contrato.domainEvents().size());
        assertInstanceOf(ContratoCriado.class, contrato.domainEvents().get(0));

        ContratoCriado evento = (ContratoCriado) contrato.domainEvents().get(0);
        assertEquals("corr-123", evento.correlationId());
        assertEquals("ContratoCriado", evento.eventType());
        assertEquals(contrato.id(), evento.contratoId());
    }

    @Test
    @DisplayName("Deve evoluir ciclo de vida: ATIVO -> ENTREGA_REGISTRADA -> CONCLUIDO com ordem de eventos estrita")
    void deveEvoluirCicloDeVidaOrdenado() {
        Contrato contrato = Contrato.criar(UUID.randomUUID(), UUID.randomUUID(), "Design UI/UX", new BigDecimal("2500.00"));
        assertEquals(StatusContrato.ATIVO, contrato.status());

        contrato.registrarEntrega("corr-entrega");
        assertEquals(StatusContrato.ENTREGA_REGISTRADA, contrato.status());

        contrato.concluir("corr-conclusao");
        assertEquals(StatusContrato.CONCLUIDO, contrato.status());

        var eventos = contrato.pullDomainEvents();
        assertEquals(3, eventos.size());
        assertEquals("ContratoCriado", eventos.get(0).eventType());
        assertEquals("EntregaRegistrada", eventos.get(1).eventType());
        assertEquals("ContratoConcluido", eventos.get(2).eventType());
        assertTrue(contrato.domainEvents().isEmpty(), "Domain events devem ser limpos após pull");
    }

    @Test
    @DisplayName("Não deve permitir concluir contrato antes da entrega estar registrada")
    void naoDeveConcluirSemEntrega() {
        Contrato contrato = Contrato.criar(UUID.randomUUID(), UUID.randomUUID(), "Consultoria", new BigDecimal("1000.00"));
        assertThrows(IllegalStateException.class, contrato::concluir);
    }
}
