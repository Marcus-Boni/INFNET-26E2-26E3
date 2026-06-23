package br.com.musicstreamer.transaction.application;

import br.com.musicstreamer.transaction.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TransactionApplicationService {
    private final TransactionRepository transactionRepository;
    private final AccountInformationService accountInformationService;

    public TransactionApplicationService(TransactionRepository transactionRepository, AccountInformationService accountInformationService) {
        this.transactionRepository = transactionRepository;
        this.accountInformationService = accountInformationService;
    }

    public UUID authorizeTransaction(AuthorizeTransactionCommand command) {
        Transaction transaction = new Transaction(command.accountId(), new Merchant(command.merchantName()), new Money(command.amount()));
        
        boolean cardActive = accountInformationService.isCardActive(command.accountId());
        
        LocalDateTime twoMinutesAgo = LocalDateTime.now().minusMinutes(2);
        List<Transaction> recentTransactions = transactionRepository.findByAccountIdAndCreatedAtAfter(command.accountId(), twoMinutesAgo);
        
        TransactionAnalyzerService analyzer = new TransactionAnalyzerService();
        analyzer.analyze(transaction, recentTransactions, cardActive);
        
        return transactionRepository.save(transaction).getId();
    }
}
