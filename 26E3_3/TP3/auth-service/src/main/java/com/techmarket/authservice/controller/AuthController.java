package com.techmarket.authservice.controller;

import com.techmarket.authservice.dto.*;
import com.techmarket.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Endpoint de Autenticação / Login.
     * Suporta /auth/login e /api/auth/login.
     */
    @PostMapping({"/auth/login", "/api/auth/login"})
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Recebida requisição de login para: {}", request.getUsername());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de Renovação de Token (Refresh).
     * Suporta /auth/refresh e /api/auth/refresh.
     */
    @PostMapping({"/auth/refresh", "/api/auth/refresh"})
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Recebida requisição de renovação de token.");
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de Cadastro de Novos Usuários.
     * Suporta /auth/register e /api/auth/register.
     */
    @PostMapping({"/auth/register", "/api/auth/register"})
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Recebida requisição de cadastro para: {}", request.getUsername());
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint para validação rápida de tokens.
     * Suporta /auth/validate e /api/auth/validate.
     */
    @GetMapping({"/auth/validate", "/api/auth/validate"})
    public ResponseEntity<TokenValidationResponse> validateToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        TokenValidationResponse response = authService.validateToken(authHeader);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint protegido para consultar o perfil do usuário atualmente autenticado.
     * Suporta /auth/me e /api/auth/me.
     */
    @GetMapping({"/auth/me", "/api/auth/me"})
    public ResponseEntity<UserProfileResponse> getCurrentUser(@AuthenticationPrincipal String username) {
        log.info("Consultando perfil do usuário autenticado: {}", username);
        UserProfileResponse profile = authService.getUserProfile(username);
        return ResponseEntity.ok(profile);
    }

    /**
     * Endpoint público informativo do microsserviço.
     * Suporta /auth/info e /api/auth/info.
     */
    @GetMapping({"/auth/info", "/api/auth/info"})
    public ResponseEntity<Map<String, Object>> getServiceInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("service", "TechMarket Authentication & Identity Service (auth-service)");
        info.put("version", "1.0.0-TP3");
        info.put("timestamp", LocalDateTime.now());
        info.put("technology", "Spring Boot 3.3.5 + Spring Security 6 + JJWT 0.12 (HMAC-SHA256)");
        info.put("defaultAccounts", Map.of(
                "admin", "admin@techmarket.com / admin123 (ROLE_ADMIN, ROLE_USER)",
                "client", "cliente@techmarket.com / senha123 (ROLE_USER)"
        ));
        info.put("endpoints", Map.of(
                "login", "POST /auth/login ou POST /api/auth/login",
                "refresh", "POST /auth/refresh ou POST /api/auth/refresh",
                "register", "POST /auth/register ou POST /api/auth/register",
                "profile", "GET /auth/me ou GET /api/auth/me (Protegido)",
                "validate", "GET /auth/validate ou GET /api/auth/validate (Público)"
        ));
        return ResponseEntity.ok(info);
    }
}
