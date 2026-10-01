package com.techmarket.authservice.service;

import com.techmarket.authservice.config.JwtProperties;
import com.techmarket.authservice.domain.RefreshToken;
import com.techmarket.authservice.domain.Role;
import com.techmarket.authservice.domain.User;
import com.techmarket.authservice.dto.*;
import com.techmarket.authservice.exception.CustomAuthenticationException;
import com.techmarket.authservice.exception.TokenRefreshException;
import com.techmarket.authservice.repository.RefreshTokenRepository;
import com.techmarket.authservice.repository.UserRepository;
import com.techmarket.authservice.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;

    /**
     * Realiza o login do usuário validando as credenciais e emitindo Access Token + Refresh Token.
     */
    public AuthResponse login(LoginRequest request) {
        log.info("Tentativa de autenticação para o usuário: {}", request.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            String authenticatedUsername = authentication.getName();
            User user = userRepository.findByUsernameOrEmail(authenticatedUsername)
                    .orElseThrow(() -> new CustomAuthenticationException("Usuário não encontrado após autenticação."));

            String accessToken = jwtTokenProvider.generateAccessToken(user);
            RefreshToken refreshToken = createRefreshToken(user.getUsername());

            Set<String> roleNames = user.getRoles().stream()
                    .map(Enum::name)
                    .collect(Collectors.toSet());

            log.info("Usuário '{}' autenticado com sucesso. Token JWT emitido.", user.getUsername());

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .tokenType("Bearer")
                    .expiresInSeconds(jwtTokenProvider.getAccessTokenExpirationInSeconds())
                    .username(user.getUsername())
                    .roles(roleNames)
                    .message("Autenticação realizada com sucesso!")
                    .build();

        } catch (BadCredentialsException ex) {
            log.warn("Falha na autenticação para '{}': credenciais inválidas.", request.getUsername());
            throw new BadCredentialsException("Credenciais inválidas: usuário ou senha incorretos.");
        }
    }

    /**
     * Renova o Access Token utilizando um Refresh Token válido (com rotação segura).
     */
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String tokenString = request.getRefreshToken();
        log.info("Solicitação de renovação com refresh token recebida.");

        RefreshToken token = refreshTokenRepository.findByToken(tokenString)
                .orElseThrow(() -> new TokenRefreshException(tokenString, "Refresh token não encontrado no servidor."));

        if (token.isRevoked()) {
            log.warn("Tentativa de uso de refresh token revogado para o usuário: {}", token.getUsername());
            throw new TokenRefreshException(tokenString, "Este refresh token foi previamente revogado.");
        }

        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.deleteByToken(tokenString);
            log.warn("Refresh token expirado para o usuário: {}", token.getUsername());
            throw new TokenRefreshException(tokenString, "O refresh token expirou. Realize login novamente.");
        }

        User user = userRepository.findByUsernameOrEmail(token.getUsername())
                .orElseThrow(() -> new CustomAuthenticationException("Usuário vinculado ao token não foi encontrado."));

        // Rotação de Refresh Token (Segurança Avançada: invalida o antigo e gera um novo)
        refreshTokenRepository.deleteByToken(tokenString);
        RefreshToken newRefreshToken = createRefreshToken(user.getUsername());

        String newAccessToken = jwtTokenProvider.generateAccessToken(user);

        Set<String> roleNames = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        log.info("Token de acesso renovado com sucesso para o usuário '{}'.", user.getUsername());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .expiresInSeconds(jwtTokenProvider.getAccessTokenExpirationInSeconds())
                .username(user.getUsername())
                .roles(roleNames)
                .message("Token renovado com sucesso via Refresh Token!")
                .build();
    }

    /**
     * Registra um novo usuário no sistema.
     */
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Nome de usuário já está em uso: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("E-mail já está em uso: " + request.getEmail());
        }

        Set<Role> roles = request.getRoles();
        if (roles == null || roles.isEmpty()) {
            roles = new HashSet<>(Collections.singletonList(Role.ROLE_USER));
        }

        User newUser = User.builder()
                .id(UUID.randomUUID().toString())
                .fullName(request.getFullName())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(newUser);
        log.info("Novo usuário cadastrado com sucesso: {}", newUser.getUsername());

        String accessToken = jwtTokenProvider.generateAccessToken(newUser);
        RefreshToken refreshToken = createRefreshToken(newUser.getUsername());

        Set<String> roleNames = newUser.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresInSeconds(jwtTokenProvider.getAccessTokenExpirationInSeconds())
                .username(newUser.getUsername())
                .roles(roleNames)
                .message("Usuário registrado e autenticado com sucesso!")
                .build();
    }

    /**
     * Valida um token JWT avulso e retorna os detalhes decodificados.
     */
    public TokenValidationResponse validateToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (token == null || !jwtTokenProvider.validateToken(token)) {
            return TokenValidationResponse.builder()
                    .valid(false)
                    .message("Token JWT inválido, expirado ou corrompido.")
                    .build();
        }

        String username = jwtTokenProvider.getUsernameFromToken(token);
        List<String> roles = jwtTokenProvider.getRolesFromToken(token);

        return TokenValidationResponse.builder()
                .valid(true)
                .username(username)
                .roles(new HashSet<>(roles))
                .message("Token JWT é válido e autêntico.")
                .build();
    }

    /**
     * Retorna os dados do perfil do usuário autenticado.
     */
    public UserProfileResponse getUserProfile(String username) {
        User user = userRepository.findByUsernameOrEmail(username)
                .orElseThrow(() -> new CustomAuthenticationException("Usuário não encontrado: " + username));

        Set<String> roleNames = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(roleNames)
                .createdAt(user.getCreatedAt())
                .build();
    }

    private RefreshToken createRefreshToken(String username) {
        RefreshToken refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID().toString())
                .username(username)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(jwtProperties.getRefreshTokenExpirationMs()))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }
}
