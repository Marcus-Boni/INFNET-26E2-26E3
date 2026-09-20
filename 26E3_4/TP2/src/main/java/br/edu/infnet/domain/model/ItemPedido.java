package br.edu.infnet.domain.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade ou Objeto de Valor pertencente exclusivamente ao Agregado Pedido.
 * Apenas o Agregado Raiz (Pedido) acessa e gerencia os itens.
 */
public class ItemPedido {

    private final UUID produtoId; // Referência a outro agregado apenas pelo ID
    private final String descricao;
    private final int quantidade;
    private final BigDecimal precoUnitario;

    public ItemPedido(UUID produtoId, String descricao, int quantidade, BigDecimal precoUnitario) {
        if (produtoId == null) {
            throw new IllegalArgumentException("ProdutoId não pode ser nulo.");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        }
        if (precoUnitario == null || precoUnitario.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Preço unitário deve ser positivo.");
        }
        this.produtoId = produtoId;
        this.descricao = Objects.requireNonNull(descricao, "Descrição não pode ser nula.");
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
    }

    public BigDecimal calcularSubtotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

    public UUID getProdutoId() {
        return produtoId;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }
}
