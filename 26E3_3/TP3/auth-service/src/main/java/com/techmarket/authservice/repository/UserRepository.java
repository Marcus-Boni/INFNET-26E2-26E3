package com.techmarket.authservice.repository;

import com.techmarket.authservice.domain.Role;
import com.techmarket.authservice.domain.User;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class UserRepository {

    private final Map<String, User> usersById = new ConcurrentHashMap<>();
    private final Map<String, String> idByUsername = new ConcurrentHashMap<>();
    private final Map<String, String> idByEmail = new ConcurrentHashMap<>();
    private final PasswordEncoder passwordEncoder;

    public UserRepository(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {
        // Usuário Administrador Padrão
        User admin = User.builder()
                .id(UUID.randomUUID().toString())
                .username("admin@techmarket.com")
                .email("admin@techmarket.com")
                .fullName("Marcus Boni (Administrador)")
                .password(passwordEncoder.encode("admin123"))
                .roles(new HashSet<>(Arrays.asList(Role.ROLE_ADMIN, Role.ROLE_USER)))
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();
        save(admin);

        // Usuário Cliente Comum Padrão
        User client = User.builder()
                .id(UUID.randomUUID().toString())
                .username("cliente@techmarket.com")
                .email("cliente@techmarket.com")
                .fullName("Cliente TechMarket")
                .password(passwordEncoder.encode("senha123"))
                .roles(new HashSet<>(Collections.singletonList(Role.ROLE_USER)))
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();
        save(client);
    }

    public User save(User user) {
        if (user.getId() == null) {
            user.setId(UUID.randomUUID().toString());
        }
        usersById.put(user.getId(), user);
        if (user.getUsername() != null) {
            idByUsername.put(user.getUsername().toLowerCase(), user.getId());
        }
        if (user.getEmail() != null) {
            idByEmail.put(user.getEmail().toLowerCase(), user.getId());
        }
        return user;
    }

    public Optional<User> findById(String id) {
        return Optional.ofNullable(usersById.get(id));
    }

    public Optional<User> findByUsernameOrEmail(String identifier) {
        if (identifier == null) {
            return Optional.empty();
        }
        String key = identifier.toLowerCase();
        String id = idByUsername.get(key);
        if (id == null) {
            id = idByEmail.get(key);
        }
        if (id != null) {
            return Optional.ofNullable(usersById.get(id));
        }
        return Optional.empty();
    }

    public boolean existsByUsername(String username) {
        return username != null && idByUsername.containsKey(username.toLowerCase());
    }

    public boolean existsByEmail(String email) {
        return email != null && idByEmail.containsKey(email.toLowerCase());
    }

    public List<User> findAll() {
        return new ArrayList<>(usersById.values());
    }
}
