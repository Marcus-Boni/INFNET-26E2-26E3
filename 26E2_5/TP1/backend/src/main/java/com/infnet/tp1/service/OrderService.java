package com.infnet.tp1.service;

import com.infnet.tp1.controller.dto.OrderRequest;
import com.infnet.tp1.domain.model.Address;
import com.infnet.tp1.domain.model.Order;
import com.infnet.tp1.domain.model.Product;
import com.infnet.tp1.domain.repository.OrderRepository;
import com.infnet.tp1.domain.repository.ProductRepository;
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

    @Autowired
    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
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
            order.addItem(product, itemReq.getQuantity());
            productRepository.save(product); // updates stock in DB
        }

        return orderRepository.save(order);
    }

    public Order shipOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado com id: " + id));
        order.ship();
        return orderRepository.save(order);
    }

    public Order cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado com id: " + id));
        
        order.cancel(); // updates status and checks constraints

        // Restore stock of items
        for (var item : order.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new IllegalStateException("Produto não encontrado ao restaurar estoque: " + item.getProductId()));
            product.increaseStock(item.getQuantity());
            productRepository.save(product);
        }

        return orderRepository.save(order);
    }
}
