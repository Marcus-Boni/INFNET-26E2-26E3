package br.com.musicstreamer.transaction.application;

import java.math.BigDecimal;
import java.util.UUID;

public record AuthorizeTransactionCommand(
    UUID accountId,
    String merchantName,
    BigDecimal amount
) {}
