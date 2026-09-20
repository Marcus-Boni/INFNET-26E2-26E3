package br.edu.infnet.messaging.saga;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Orquestrador de Sagas para gerenciar transações distribuídas em microsserviços.
 * Executa as etapas sequencialmente e, caso ocorra qualquer falha, aciona
 * automaticamente as transações compensatórias em ordem reversa (LIFO).
 */
public class OrderSagaOrchestrator {

    private final List<SagaStep> steps = new ArrayList<>();

    public OrderSagaOrchestrator addStep(SagaStep step) {
        this.steps.add(step);
        return this;
    }

    /**
     * Executa o fluxo coordenado da Saga.
     * @return true se todas as etapas foram concluídas com sucesso; false se houve compensação.
     */
    public boolean executeSaga() {
        Deque<SagaStep> executedSteps = new ArrayDeque<>();
        System.out.println("========== INICIANDO ORQUESTRAÇÃO DE SAGA ==========");

        for (SagaStep step : steps) {
            System.out.printf("[SAGA] Executando etapa: %s...\n", step.getName());
            boolean success = step.execute();

            if (success) {
                executedSteps.push(step);
                System.out.printf(" -> Etapa %s concluída com sucesso.\n", step.getName());
            } else {
                System.err.printf("[SAGA FALHA] Erro na etapa %s. Iniciando compensação reversa!\n", step.getName());
                rollback(executedSteps);
                return false;
            }
        }

        System.out.println("========== SAGA CONCLUÍDA COM SUCESSO! ==========");
        return true;
    }

    /**
     * Executa o rollback semântico acionando as transações compensatórias
     * em ordem estritamente reversa.
     */
    private void rollback(Deque<SagaStep> executedSteps) {
        System.out.println("----- EXECUTANDO TRANSAÇÕES COMPENSATÓRIAS (ROLLBACK SEMÂNTICO) -----");
        while (!executedSteps.isEmpty()) {
            SagaStep step = executedSteps.pop();
            try {
                System.out.printf("[COMPENSAÇÃO] Revertendo efeitos da etapa: %s...\n", step.getName());
                step.compensate();
                System.out.printf(" -> Compensação de %s concluída.\n", step.getName());
            } catch (Exception ex) {
                System.err.printf("[ALERTA CRÍTICO] Falha ao compensar etapa %s: %s\n", step.getName(), ex.getMessage());
            }
        }
        System.out.println("----- COMPENSAÇÃO FINALIZADA. CONSISTÊNCIA RESTAURADA. -----");
    }
}
