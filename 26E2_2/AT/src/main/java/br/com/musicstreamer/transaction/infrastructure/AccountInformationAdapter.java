package br.com.musicstreamer.transaction.infrastructure;

import br.com.musicstreamer.account.domain.Account;
import br.com.musicstreamer.account.domain.AccountRepository;
import br.com.musicstreamer.transaction.domain.AccountInformationService;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AccountInformationAdapter implements AccountInformationService {
    private final AccountRepository accountRepository;

    public AccountInformationAdapter(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public boolean isCardActive(UUID accountId) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        return account.getCreditCard() != null && account.getCreditCard().isActive();
    }
}
