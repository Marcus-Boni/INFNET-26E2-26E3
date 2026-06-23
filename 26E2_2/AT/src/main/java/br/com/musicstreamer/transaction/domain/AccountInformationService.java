package br.com.musicstreamer.transaction.domain;

import java.util.UUID;

public interface AccountInformationService {
    boolean isCardActive(UUID accountId);
}
