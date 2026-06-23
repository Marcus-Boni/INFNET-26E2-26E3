package br.com.musicstreamer.account.presentation;

import br.com.musicstreamer.account.application.AccountApplicationService;
import br.com.musicstreamer.account.application.CreateAccountCommand;
import br.com.musicstreamer.account.domain.Plan;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountApplicationService service;

    public AccountController(AccountApplicationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Void> createAccount(@RequestBody CreateAccountCommand command) {
        UUID accountId = service.createAccount(command);
        return ResponseEntity.created(URI.create("/api/accounts/" + accountId)).build();
    }

    @PostMapping("/{accountId}/subscriptions")
    public ResponseEntity<Void> subscribe(@PathVariable UUID accountId, @RequestParam Plan plan) {
        service.subscribe(accountId, plan);
        return ResponseEntity.ok().build();
    }
}
