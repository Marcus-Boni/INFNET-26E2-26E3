package com.infnet.tp2.service;

import com.infnet.tp2.controller.dto.OrderRequest;
import com.infnet.tp2.domain.model.Address;
import com.infnet.tp2.domain.model.AuditLog;
import com.infnet.tp2.domain.model.Order;
import com.infnet.tp2.domain.model.OrderStatus;
import com.infnet.tp2.domain.model.Product;
import com.infnet.tp2.domain.repository.OrderRepository;
import com.infnet.tp2.domain.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final AuditLogService auditLogService;

    @Autowired
    public OrderService(OrderRepository orderRepository, ProductRepository productRepository, AuditLogService auditLogService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orderRepository.findAllWithItems();
    }

    @Transactional(readOnly = true)
    public Optional<Order> findById(Long id) {
        return orderRepository.findWithItemsById(id);
    }

    @Transactional(readOnly = true)
    public List<Order> findByCustomerEmail(String email) {
        return orderRepository.findByCustomerEmailOrderByCreatedAtDesc(email);
    }

    @Transactional(readOnly = true)
    public List<Order> findByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public Order createOrder(OrderRequest request) {
        Address address = new Address(
                request.getStreet(),
                request.getCity(),
                request.getState(),
                request.getZipCode()
        );

        Order order = Order.builder()
                .customerEmail(request.getCustomerEmail())
                .shippingAddress(address)
                .build();
        order.initialize();

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("O pedido deve conter pelo menos um item.");
        }

        for (var itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado com id: " + itemReq.getProductId()));
            
            int oldStock = product.getStock();
            order.addItem(product, itemReq.getQuantity());
            productRepository.save(product);

            auditLogService.logChange(
                    "Product",
                    product.getId(),
                    "STOCK_CHANGE",
                    "Estoque reduzido devido ao pedido para '" + request.getCustomerEmail() + "'",
                    String.valueOf(oldStock),
                    String.valueOf(product.getStock())
            );
        }

        Order savedOrder = orderRepository.save(order);

        auditLogService.logChange(
                "Order",
                savedOrder.getId(),
                "CREATE",
                "Pedido registrado com " + savedOrder.getItems().size() + " itens por " + savedOrder.getCustomerEmail(),
                null,
                "Status: PENDING, Total: R$ " + savedOrder.getTotalPrice()
        );

        return savedOrder;
    }

    public Order shipOrder(Long id) {
        Order order = orderRepository.findWithItemsById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado com id: " + id));
        
        OrderStatus oldStatus = order.getStatus();
        order.ship();
        Order updated = orderRepository.save(order);

        auditLogService.logChange(
                "Order",
                id,
                "STATUS_CHANGE",
                "Pedido enviado para transporte",
                oldStatus.name(),
                updated.getStatus().name()
        );

        return updated;
    }

    public Order cancelOrder(Long id) {
        Order order = orderRepository.findWithItemsById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado com id: " + id));
        
        OrderStatus oldStatus = order.getStatus();
        order.cancel();

        for (var item : order.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new IllegalStateException("Produto não encontrado ao restaurar estoque: " + item.getProductId()));
            int oldStock = product.getStock();
            product.increaseStock(item.getQuantity());
            productRepository.save(product);

            auditLogService.logChange(
                    "Product",
                    product.getId(),
                    "STOCK_CHANGE",
                    "Estoque restaurado devido ao cancelamento do Pedido #" + id,
                    String.valueOf(oldStock),
                    String.valueOf(product.getStock())
            );
        }

        Order updated = orderRepository.save(order);

        auditLogService.logChange(
                "Order",
                id,
                "STATUS_CHANGE",
                "Pedido cancelado pelo cliente",
                oldStatus.name(),
                updated.getStatus().name()
        );

        return updated;
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getOrderHistory(Long id) {
        return auditLogService.getHistoryForEntity("Order", id);
    }
}
