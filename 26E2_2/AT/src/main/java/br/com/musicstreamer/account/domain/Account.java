package br.com.musicstreamer.account.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {
    @Id
    private UUID id;

    private String name;
    private String email;

    @Embedded
    private CreditCard creditCard;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "account_id")
    private List<Subscription> subscriptions = new ArrayList<>();

    public Account(String name, String email, CreditCard creditCard) {
        if (creditCard == null || !creditCard.isValid()) {
            throw new IllegalArgumentException("O usuário deve ter um cartão de crédito válido");
        }
        this.id = UUID.randomUUID();
        this.name = name;
        this.email = email;
        this.creditCard = creditCard;
    }

    public void subscribe(Plan plan) {
        if (hasActiveSubscription()) {
            throw new IllegalStateException("O usuário pode ter somente um plano ativo");
        }
        this.subscriptions.add(new Subscription(plan));
    }

    public boolean hasActiveSubscription() {
        return subscriptions.stream().anyMatch(Subscription::isActive);
    }
}
