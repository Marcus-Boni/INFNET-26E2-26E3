package com.infnet.tp2.service;

import com.infnet.tp2.controller.dto.OrderItemRequest;
import com.infnet.tp2.controller.dto.OrderRequest;
import com.infnet.tp2.domain.model.AuditLog;
import com.infnet.tp2.domain.model.Order;
import com.infnet.tp2.domain.model.OrderStatus;
import com.infnet.tp2.domain.model.Product;
import com.infnet.tp2.domain.repository.AuditLogRepository;
import com.infnet.tp2.domain.repository.OrderRepository;
import com.infnet.tp2.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        auditLogRepository.deleteAll();

        testProduct = productRepository.save(Product.builder()
                .name("Notebook Gamer")
                .description("Intel i7, 16GB RAM, RTX 3060")
                .price(new BigDecimal("5000.00"))
                .stock(10)
                .build());
    }

    @Test
    @DisplayName("Deve criar pedido com sucesso, debitar estoque e gerar log de auditoria")
    void shouldCreateOrderAndDebitStockAndAudit() {
        OrderRequest request = new OrderRequest();
        request.setCustomerEmail("usuario@empresa.com");
        request.setStreet("Av Paulista, 1000");
        request.setCity("São Paulo");
        request.setState("SP");
        request.setZipCode("01310-100");

        OrderItemRequest itemReq = new OrderItemRequest();
        itemReq.setProductId(testProduct.getId());
        itemReq.setQuantity(2);
        request.setItems(List.of(itemReq));

        Order createdOrder = orderService.createOrder(request);

        assertThat(createdOrder.getId()).isNotNull();
        assertThat(createdOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(createdOrder.getTotalPrice()).isEqualByComparingTo("10000.00");

        // Verify stock deduction
        Product updatedProduct = productRepository.findById(testProduct.getId()).orElseThrow();
        assertThat(updatedProduct.getStock()).isEqualTo(8);

        // Verify audit logs
        List<AuditLog> orderLogs = auditLogRepository.findByEntityNameAndEntityIdOrderByTimestampDesc("Order", createdOrder.getId());
        assertThat(orderLogs).hasSize(1);
        assertThat(orderLogs.get(0).getAction()).isEqualTo("CREATE");
    }

    @Test
    @DisplayName("Deve cancelar pedido e restaurar o estoque do produto")
    void shouldCancelOrderAndRestoreStock() {
        OrderRequest request = new OrderRequest();
        request.setCustomerEmail("usuario@empresa.com");
        request.setStreet("Av Paulista, 1000");
        request.setCity("São Paulo");
        request.setState("SP");
        request.setZipCode("01310-100");

        OrderItemRequest itemReq = new OrderItemRequest();
        itemReq.setProductId(testProduct.getId());
        itemReq.setQuantity(3);
        request.setItems(List.of(itemReq));

        Order order = orderService.createOrder(request);
        Order cancelled = orderService.cancelOrder(order.getId());

        assertThat(cancelled.getStatus()).isEqualTo(OrderStatus.CANCELLED);

        Product restoredProduct = productRepository.findById(testProduct.getId()).orElseThrow();
        assertThat(restoredProduct.getStock()).isEqualTo(10);
    }
}
