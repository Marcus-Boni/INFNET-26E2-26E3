package br.com.musicstreamer.account.application;

import br.com.musicstreamer.account.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class AccountApplicationService {
    private final AccountRepository accountRepository;

    public AccountApplicationService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public UUID createAccount(CreateAccountCommand command) {
        CreditCard creditCard = new CreditCard(command.cardNumber(), command.cardLimit(), command.cardActive());
        Account account = new Account(command.name(), command.email(), creditCard);
        return accountRepository.save(account).getId();
    }

    public void subscribe(UUID accountId, Plan plan) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        account.subscribe(plan);
        accountRepository.save(account);
    }
}
