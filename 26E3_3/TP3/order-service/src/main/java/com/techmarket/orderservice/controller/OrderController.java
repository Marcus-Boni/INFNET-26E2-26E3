package com.techmarket.orderservice.controller;

import com.techmarket.orderservice.dto.CreateOrderRequest;
import com.techmarket.orderservice.dto.OrderResponse;
import com.techmarket.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // =========================================================================
    // ENDPOINTS PÚBLICOS (Não exigem Token JWT)
    // =========================================================================

    /**
     * Endpoint público para verificar o status e informações do microsserviço.
     * Suporta /api/pedidos/public/info e /orders/public/info.
     */
    @GetMapping({"/api/pedidos/public/info", "/orders/public/info"})
    public ResponseEntity<Map<String, Object>> getPublicInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("service", "TechMarket Order Service (order-service)");
        info.put("description", "Microsserviço de Gestão de Pedidos com Rotas Protegidas por JWT");
        info.put("version", "1.0.0-TP3");
        info.put("timestamp", LocalDateTime.now());
        info.put("security", "Spring Security 6 Stateless + JJWT 0.12 Bearer Token Validator");
        info.put("publicEndpoints", List.of(
                "GET /api/pedidos/public/info",
                "GET /orders/public/info",
                "GET /actuator/health"
        ));
        info.put("protectedEndpoints", List.of(
                "GET /api/pedidos (ou /orders) - Requer 'Authorization: Bearer <token>'",
                "POST /api/pedidos (ou /orders) - Requer 'Authorization: Bearer <token>'",
                "GET /api/pedidos/{id} (ou /orders/{id}) - Requer 'Authorization: Bearer <token>'"
        ));
        info.put("instruction", "Para acessar rotas protegidas, autentique-se primeiro no auth-service (POST http://localhost:8080/auth/login) e envie o token recebido no header 'Authorization: Bearer <token>'.");
        return ResponseEntity.ok(info);
    }

    // =========================================================================
    // ENDPOINTS PROTEGIDOS (Exigem Token JWT Válido)
    // =========================================================================

    /**
     * Endpoint protegido para listagem de pedidos.
     * Suporta GET /api/pedidos e GET /orders.
     */
    @GetMapping({"/api/pedidos", "/orders"})
    public ResponseEntity<List<OrderResponse>> getAllOrders(Authentication authentication) {
        String username = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        log.info("Acesso autorizado concedido para '{}' listar pedidos.", username);
        List<OrderResponse> orders = orderService.getAllOrders(username, isAdmin);
        return ResponseEntity.ok(orders);
    }

    /**
     * Endpoint protegido para buscar pedido por ID.
     * Suporta GET /api/pedidos/{id} e GET /orders/{id}.
     */
    @GetMapping({"/api/pedidos/{id}", "/orders/{id}"})
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable String id, Authentication authentication) {
        String username = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        log.info("Acesso autorizado concedido para '{}' consultar pedido #{}.", username, id);
        OrderResponse order = orderService.getOrderById(id, username, isAdmin);
        return ResponseEntity.ok(order);
    }

    /**
     * Endpoint protegido para criar novo pedido.
     * Suporta POST /api/pedidos e POST /orders.
     */
    @PostMapping({"/api/pedidos", "/orders"})
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request,
                                                    Authentication authentication) {
        String username = authentication.getName();
        log.info("Acesso autorizado concedido para '{}' cadastrar novo pedido.", username);

        OrderResponse created = orderService.createOrder(request, username, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
