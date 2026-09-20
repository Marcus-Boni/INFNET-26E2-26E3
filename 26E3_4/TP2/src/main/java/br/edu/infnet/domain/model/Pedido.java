package br.edu.infnet.domain.model;

import br.edu.infnet.domain.event.DomainEvent;
import br.edu.infnet.domain.event.PedidoPagoEvent;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Questão 6 e Questão 7:
 * Entidade que atua como Raiz do Agregado (Aggregate Root).
 * 
 * - Faz referência a outros agregados (Cliente, Produto) exclusivamente através do seu ID (Questão 6).
 * - Possui método de negócio ("pagar") que valida invariantes e prevê a publicação de evento de domínio (Questão 7).
 */
public class Pedido {

    private final UUID id;
    
    // Questão 6: Referência a outro Agregado (Cliente) APENAS pelo ID (desacoplamento transacional)
    private final UUID clienteId;
    
    private final List<ItemPedido> itens = new ArrayList<>();
    private StatusPedido status;
    private BigDecimal valorTotal;

    // Fila interna de eventos de domínio gerados pelas operações do agregado
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    public Pedido(UUID id, UUID clienteId) {
        this.id = Objects.requireNonNull(id, "ID do pedido não pode ser nulo.");
        this.clienteId = Objects.requireNonNull(clienteId, "ClienteID não pode ser nulo.");
        this.status = StatusPedido.CRIADO;
        this.valorTotal = BigDecimal.ZERO;
    }

    public void adicionarItem(UUID produtoId, String descricao, int quantidade, BigDecimal precoUnitario) {
        if (this.status != StatusPedido.CRIADO) {
            throw new IllegalStateException("Não é possível adicionar itens a um pedido que não esteja no estado CRIADO.");
        }
        ItemPedido item = new ItemPedido(produtoId, descricao, quantidade, precoUnitario);
        this.itens.add(item);
        this.valorTotal = this.valorTotal.add(item.calcularSubtotal());
    }

    /**
     * Questão 7: Método de negócio com validação de invariantes e previsão de publicação de evento de domínio.
     * 
     * @param valorPago Valor recebido para a quitação do pedido.
     */
    public void pagar(BigDecimal valorPago) {
        // Validação de Invariantes de Negócio
        if (this.status == StatusPedido.PAGO) {
            throw new IllegalStateException("O pedido já se encontra quitado/pago.");
        }
        if (this.status == StatusPedido.CANCELADO) {
            throw new IllegalStateException("Não é permitido pagar um pedido previamente cancelado.");
        }
        if (this.itens.isEmpty()) {
            throw new IllegalStateException("O pedido não pode ser pago pois não possui itens cadastrados.");
        }
        if (valorPago == null || valorPago.compareTo(this.valorTotal) < 0) {
            throw new IllegalArgumentException("O valor fornecido é insuficiente para cobrir o total do pedido.");
        }

        // Alteração de estado interno do agregado
        this.status = StatusPedido.PAGO;

        // Previsão e registro do evento de domínio para futura publicação transacional
        PedidoPagoEvent evento = new PedidoPagoEvent(this.id, this.clienteId, valorPago);
        this.domainEvents.add(evento);
    }

    public UUID getId() {
        return id;
    }

    public UUID getClienteId() {
        return clienteId;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public List<ItemPedido> getItens() {
        return Collections.unmodifiableList(itens);
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> recordedEvents = new ArrayList<>(this.domainEvents);
        this.domainEvents.clear();
        return Collections.unmodifiableList(recordedEvents);
    }
}
