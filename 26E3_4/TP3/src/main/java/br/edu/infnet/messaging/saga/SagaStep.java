package br.edu.infnet.messaging.saga;

/**
 * Interface funcional que define uma etapa de uma Saga com sua respectiva
 * transação local e sua correspondente transação compensatória.
 */
public interface SagaStep {

    String getName();

    /**
     * Executa a transação direta local no microsserviço.
     */
    boolean execute();

    /**
     * Executa a transação compensatória em caso de falha nas etapas posteriores,
     * desfazendo semanticamente os efeitos já persistidos.
     */
    void compensate();
}
