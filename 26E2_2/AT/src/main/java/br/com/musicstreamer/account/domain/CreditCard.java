package br.com.musicstreamer.account.domain;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CreditCard {
    private String cardNumber;
    private String limitAmount;
    private boolean active;

    public boolean isValid() {
        return cardNumber != null && !cardNumber.isBlank();
    }
}
