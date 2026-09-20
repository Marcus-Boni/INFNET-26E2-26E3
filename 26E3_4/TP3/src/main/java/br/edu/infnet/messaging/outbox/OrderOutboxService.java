package br.edu.infnet.messaging.outbox;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Demonstração do Transactional Outbox Pattern para resolver o Dual-Write Problem.
 * Salva a alteração de estado no banco de dados e registra a mensagem de evento
 * na tabela Outbox dentro da MESMA transação atômica ACID local.
 */
public class OrderOutboxService {

    // Simulação das tabelas do banco de dados relacional
    private final List<String> orderTable = Collections.synchronizedList(new ArrayList<>());
    private final List<OutboxMessage> outboxTable = Collections.synchronizedList(new ArrayList<>());

    /**
     * Cria e persiste o pedido gerando o evento na tabela outbox atomicamente.
     * Em ambiente Spring Framework, este método seria anotado com @Transactional.
     */
    public void createOrder(UUID orderId, UUID customerId, BigDecimal amount) {
        // Início da Transação ACID Local
        try {
            // 1. Persiste o estado da entidade na tabela de pedidos
            String orderRecord = String.format("ORDER[id=%s, customer=%s, total=%s, status=CREATED]", 
                    orderId, customerId, amount);
            orderTable.add(orderRecord);
            System.out.println("[DB TRANSACTION] Pedido gravado com sucesso na tb_pedidos: " + orderId);

            // 2. Prepara o evento de domínio no formato JSON
            String jsonPayload = String.format(
                "{\"orderId\":\"%s\",\"customerId\":\"%s\",\"amount\":%s,\"event\":\"ORDER_CREATED\"}",
                orderId, customerId, amount
            );

            // 3. Persiste o evento na tabela tb_outbox DENTRO DA MESMA TRANSAÇÃO
            OutboxMessage outboxMessage = new OutboxMessage("Order", orderId, "OrderCreatedEvent", jsonPayload);
            outboxTable.add(outboxMessage);
            System.out.println("[DB TRANSACTION] Mensagem gravada atomicamente na tb_outbox: " + outboxMessage.getId());

            // Commit da Transação ACID Local (Ambos são confirmados juntos!)
            System.out.println("[DB TRANSACTION] COMMIT realizado com sucesso. Dual-Write eliminado.");
        } catch (Exception ex) {
            // Rollback da Transação ACID: se o banco falhar, nem pedido nem outbox são salvos
            System.err.println("[DB TRANSACTION] ROLLBACK executado devido a erro: " + ex.getMessage());
            throw ex;
        }
    }

    /**
     * Processo desacoplado em segundo plano (Poller ou Debezium CDC)
     * que lê a tabela outbox e envia as mensagens para o Message Broker.
     */
    public void publishPendingOutboxMessages() {
        System.out.println("[OUTBOX PUBLISHER] Verificando mensagens pendentes na tb_outbox...");
        for (OutboxMessage message : outboxTable) {
            if (message.getStatus() == OutboxMessage.Status.PENDING) {
                // Envia para o Broker (ex.: Kafka ou RabbitMQ)
                System.out.printf(" -> Publicando evento no broker: [%s] -> Payload: %s\n", 
                        message.getEventType(), message.getPayload());
                
                // Marca como publicada após confirmação do broker
                message.markAsPublished();
            }
        }
    }
}
