package br.com.musicstreamer.transaction.domain;

import java.util.List;

public class TransactionAnalyzerService {
    public void analyze(Transaction transaction, List<Transaction> recentTransactions, boolean cardActive) {
        if (!cardActive) {
            throw new IllegalStateException("cartão não ativo");
        }
        
        if (recentTransactions.size() >= 3) {
            throw new IllegalStateException("alta-frequência-pequeno-intervalo");
        }
        
        long similarCount = recentTransactions.stream()
            .filter(t -> t.isSimilarTo(transaction))
            .count();
            
        if (similarCount >= 2) {
            throw new IllegalStateException("transação duplicada");
        }
        
        transaction.authorize();
    }
}
