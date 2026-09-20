package br.edu.infnet.messaging.idempotency;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Serviço que implementa o padrão Idempotent Consumer.
 * Garante que mensagens duplicadas entregues pelo broker (semântica At-Least-Once)
 * sejam ignoradas sem causar efeitos colaterais duplicados no negócio.
 */
public class IdempotentConsumerService {

    // Simulação do repositório/tabela de deduplicação no banco relacional
    private final Set<UUID> processedMessageStore = Collections.synchronizedSet(new HashSet<>());

    /**
     * Processa uma mensagem garantindo a idempotência.
     *
     * @param messageId   Identificador global único da mensagem (UUID/CorrelationId)
     * @param messageType Tipo de evento ou comando recebido
     * @param payload     Dados da carga útil da mensagem
     * @return true se a mensagem foi processada; false se era duplicada e foi descartada
     */
    public boolean processMessage(UUID messageId, String messageType, String payload) {
        // 1. Verificação prévia: a mensagem já foi processada anteriormente?
        if (isAlreadyProcessed(messageId)) {
            System.out.printf("[IDEMPOTÊNCIA] Mensagem duplicada detectada: %s. Descartando silenciosamente.\n", messageId);
            return false;
        }

        // 2. Executa a operação de negócio dentro de uma fronteira transacional
        try {
            executeBusinessLogic(payload);

            // 3. Registra a mensagem como processada (persistência atômica no banco)
            markAsProcessed(new ProcessedMessage(messageId, messageType));
            System.out.printf("[IDEMPOTÊNCIA] Mensagem %s processada e registrada com sucesso.\n", messageId);
            return true;
        } catch (Exception ex) {
            System.err.printf("[ERRO] Falha ao processar mensagem %s: %s\n", messageId, ex.getMessage());
            throw ex;
        }
    }

    private boolean isAlreadyProcessed(UUID messageId) {
        return processedMessageStore.contains(messageId);
    }

    private void markAsProcessed(ProcessedMessage message) {
        processedMessageStore.add(message.getMessageId());
    }

    private void executeBusinessLogic(String payload) {
        // Operação de negócio, por exemplo, faturar pedido ou creditar conta
        System.out.printf(" -> Executando lógica de negócio para payload: %s\n", payload);
    }
}
