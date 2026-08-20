package com.infnet.tp3.service;

import com.infnet.tp3.client.ShippingClient;
import com.infnet.tp3.client.dto.CreateShipmentRequest;
import com.infnet.tp3.client.dto.ShipmentDetailsDto;
import com.infnet.tp3.client.dto.UpdateShipmentStatusRequest;
import com.infnet.tp3.controller.dto.OrderItemRequest;
import com.infnet.tp3.controller.dto.OrderRequest;
import com.infnet.tp3.domain.model.*;
import com.infnet.tp3.domain.repository.AuditLogRepository;
import com.infnet.tp3.domain.repository.OrderRepository;
import com.infnet.tp3.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
class OrderServiceDistributedTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @MockBean
    private ShippingClient shippingClient;

    private Product product;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        auditLogRepository.deleteAll();
        productRepository.deleteAll();

        product = productRepository.save(Product.builder()
                .name("Monitor Gamer 144Hz")
                .description("Monitor Curvo")
                .price(new BigDecimal("1500.00"))
                .stock(10)
                .build());

        when(shippingClient.createShipment(any(CreateShipmentRequest.class)))
                .thenReturn(ShipmentDetailsDto.builder()
                        .trackingNumber("NX-998877-BR")
                        .carrier("Nexus Express Air")
                        .status("CREATED")
                        .build());
    }

    @Test
    @DisplayName("Deve criar pedido, integrar com microsserviço de frete via Feign e registrar log de auditoria")
    void shouldCreateOrderAndIntegrateWithShippingService() {
        OrderRequest request = OrderRequest.builder()
                .customerEmail("comprador@infnet.com")
                .street("Av. Presidente Vargas, 100")
                .city("Rio de Janeiro")
                .state("RJ")
                .zipCode("20071-001")
                .carrier("Nexus Express Air")
                .serviceType("EXPRESS")
                .shippingCost(new BigDecimal("29.90"))
                .estimatedDeliveryDays(2)
                .items(List.of(OrderItemRequest.builder()
                        .productId(product.getId())
                        .quantity(2)
                        .build()))
                .build();

        Order order = orderService.createOrder(request);

        assertThat(order).isNotNull();
        assertThat(order.getId()).isNotNull();
        assertThat(order.getTrackingNumber()).isEqualTo("NX-998877-BR");
        assertThat(order.getShippingCarrier()).isEqualTo("Nexus Express Air");
        assertThat(order.getShippingCost()).isEqualByComparingTo(new BigDecimal("29.90"));
        assertThat(order.getItemsTotal()).isEqualByComparingTo(new BigDecimal("3000.00"));
        assertThat(order.getTotalPrice()).isEqualByComparingTo(new BigDecimal("3029.90"));

        // Verifica que o estoque do produto foi decrementado
        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertThat(updatedProduct.getStock()).isEqualTo(8);

        // Verifica que a chamada ao Feign client foi executada
        verify(shippingClient, times(1)).createShipment(any(CreateShipmentRequest.class));

        // Verifica que os logs de auditoria foram persistidos
        List<AuditLog> orderLogs = auditLogRepository.findByEntityNameOrderByTimestampDesc("Order");
        assertThat(orderLogs).isNotEmpty();
    }

    @Test
    @DisplayName("Deve despachar pedido e notificar microsserviço de frete sobre atualização de status")
    void shouldShipOrderAndNotifyShippingService() {
        OrderRequest request = OrderRequest.builder()
                .customerEmail("despacho@infnet.com")
                .street("Rua do Ouvidor, 10")
                .city("Rio de Janeiro")
                .state("RJ")
                .zipCode("20040-030")
                .carrier("LogBrasil Rodoviário")
                .shippingCost(new BigDecimal("15.00"))
                .estimatedDeliveryDays(3)
                .items(List.of(OrderItemRequest.builder()
                        .productId(product.getId())
                        .quantity(1)
                        .build()))
                .build();

        Order created = orderService.createOrder(request);

        when(shippingClient.updateShipmentStatus(eq("NX-998877-BR"), any(UpdateShipmentStatusRequest.class)))
                .thenReturn(ShipmentDetailsDto.builder()
                        .trackingNumber("NX-998877-BR")
                        .status("DISPATCHED")
                        .build());

        Order shipped = orderService.shipOrder(created.getId());

        assertThat(shipped.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        verify(shippingClient, times(1)).updateShipmentStatus(eq("NX-998877-BR"), any(UpdateShipmentStatusRequest.class));
    }
}
