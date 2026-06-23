package br.com.musicstreamer.account.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscription {
    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    private Plan plan;

    private boolean active;
    private LocalDateTime createdAt;

    public Subscription(Plan plan) {
        this.id = UUID.randomUUID();
        this.plan = plan;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    public void cancel() {
        this.active = false;
    }
}
