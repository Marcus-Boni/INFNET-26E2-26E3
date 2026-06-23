package br.com.musicstreamer.transaction.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TransactionAnalyzerServiceTest {

    @Test
    void shouldAuthorizeValidTransaction() {
        TransactionAnalyzerService analyzer = new TransactionAnalyzerService();
        Transaction t = new Transaction(UUID.randomUUID(), new Merchant("Spotify"), new Money(new BigDecimal("19.90")));
        
        assertDoesNotThrow(() -> analyzer.analyze(t, List.of(), true));
        assertTrue(t.isAuthorized());
    }

    @Test
    void shouldRejectWhenCardNotActive() {
        TransactionAnalyzerService analyzer = new TransactionAnalyzerService();
        Transaction t = new Transaction(UUID.randomUUID(), new Merchant("Spotify"), new Money(new BigDecimal("19.90")));
        
        Exception e = assertThrows(IllegalStateException.class, () -> analyzer.analyze(t, List.of(), false));
        assertEquals("cartão não ativo", e.getMessage());
    }

    @Test
    void shouldRejectHighFrequencySmallInterval() {
        TransactionAnalyzerService analyzer = new TransactionAnalyzerService();
        Transaction t = new Transaction(UUID.randomUUID(), new Merchant("Spotify"), new Money(new BigDecimal("19.90")));
        
        List<Transaction> recent = List.of(
            new Transaction(UUID.randomUUID(), new Merchant("Netflix"), new Money(new BigDecimal("10"))),
            new Transaction(UUID.randomUUID(), new Merchant("Amazon"), new Money(new BigDecimal("10"))),
            new Transaction(UUID.randomUUID(), new Merchant("Steam"), new Money(new BigDecimal("10")))
        );

        Exception e = assertThrows(IllegalStateException.class, () -> analyzer.analyze(t, recent, true));
        assertEquals("alta-frequência-pequeno-intervalo", e.getMessage());
    }

    @Test
    void shouldRejectDuplicatedTransactions() {
        TransactionAnalyzerService analyzer = new TransactionAnalyzerService();
        Transaction t = new Transaction(UUID.randomUUID(), new Merchant("Spotify"), new Money(new BigDecimal("19.90")));
        
        List<Transaction> recent = List.of(
            new Transaction(UUID.randomUUID(), new Merchant("Spotify"), new Money(new BigDecimal("19.90"))),
            new Transaction(UUID.randomUUID(), new Merchant("Spotify"), new Money(new BigDecimal("19.90")))
        );

        Exception e = assertThrows(IllegalStateException.class, () -> analyzer.analyze(t, recent, true));
        assertEquals("transação duplicada", e.getMessage());
    }
}
