package br.com.musicstreamer.transaction.presentation;

import br.com.musicstreamer.transaction.application.AuthorizeTransactionCommand;
import br.com.musicstreamer.transaction.application.TransactionApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionApplicationService service;

    public TransactionController(TransactionApplicationService service) {
        this.service = service;
    }

    @PostMapping("/authorize")
    public ResponseEntity<?> authorize(@RequestBody AuthorizeTransactionCommand command) {
        try {
            UUID id = service.authorizeTransaction(command);
            return ResponseEntity.ok().body("Transaction authorized with ID: " + id);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body("Transaction rejected: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
