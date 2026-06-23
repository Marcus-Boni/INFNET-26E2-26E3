package br.com.musicstreamer.transaction.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction {
    @Id
    private UUID id;

    private UUID accountId;

    @Embedded
    private Merchant merchant;

    @Embedded
    private Money amount;

    private LocalDateTime createdAt;
    
    private boolean authorized;

    public Transaction(UUID accountId, Merchant merchant, Money amount) {
        this.id = UUID.randomUUID();
        this.accountId = accountId;
        this.merchant = merchant;
        this.amount = amount;
        this.createdAt = LocalDateTime.now();
        this.authorized = false;
    }

    public void authorize() {
        this.authorized = true;
    }
    
    public void reject() {
        this.authorized = false;
    }
    
    public boolean isSimilarTo(Transaction other) {
        return this.merchant.equals(other.getMerchant()) && this.amount.equals(other.getAmount());
    }
}
