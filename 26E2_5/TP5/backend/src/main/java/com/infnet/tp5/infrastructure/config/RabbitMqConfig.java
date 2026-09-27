package com.infnet.tp5.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMqConfig {

    // --- Nomes das Exchanges ---
    public static final String EXCHANGE_ORDER = "nexus.order.exchange";
    public static final String EXCHANGE_SHIPPING = "nexus.shipping.exchange";
    public static final String EXCHANGE_DLX = "nexus.dlx.exchange";

    // --- Routing Keys ---
    public static final String ROUTING_KEY_ORDER_CREATED = "order.created";
    public static final String ROUTING_KEY_ORDER_DISPATCHED = "order.dispatched";
    public static final String ROUTING_KEY_ORDER_CANCELLED = "order.cancelled";
    public static final String ROUTING_KEY_SHIPPING_CREATED = "shipping.created";
    public static final String ROUTING_KEY_SHIPPING_STATUS_UPDATED = "shipping.status-updated";
    public static final String ROUTING_KEY_ORDER_DLQ = "order.dlq";
    public static final String ROUTING_KEY_SHIPPING_DLQ = "shipping.dlq";

    // --- Nomes das Filas (Consumidas pelo Backend de Pedidos) ---
    public static final String QUEUE_ORDER_SHIPMENT_CREATED = "order.shipment-created.queue";
    public static final String QUEUE_ORDER_SHIPMENT_STATUS_UPDATED = "order.shipment-status-updated.queue";
    public static final String QUEUE_ORDER_DLQ = "order.dead-letter.queue";

    // --- Nomes das Filas (Consumidas pelo Shipping Service) ---
    public static final String QUEUE_SHIPPING_ORDER_CREATED = "shipping.order-created.queue";
    public static final String QUEUE_SHIPPING_ORDER_DISPATCHED = "shipping.order-dispatched.queue";
    public static final String QUEUE_SHIPPING_ORDER_CANCELLED = "shipping.order-cancelled.queue";
    public static final String QUEUE_SHIPPING_DLQ = "shipping.dead-letter.queue";

    // ==========================================
    // 1. Definição das Exchanges (Topic Exchanges)
    // ==========================================
    @Bean
    public TopicExchange orderExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE_ORDER).durable(true).build();
    }

    @Bean
    public TopicExchange shippingExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE_SHIPPING).durable(true).build();
    }

    @Bean
    public TopicExchange deadLetterExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE_DLX).durable(true).build();
    }

    // ==========================================
    // 2. Definição das Filas com Dead Letter Exchange
    // ==========================================
    @Bean
    public Queue orderShipmentCreatedQueue() {
        return QueueBuilder.durable(QUEUE_ORDER_SHIPMENT_CREATED)
                .deadLetterExchange(EXCHANGE_DLX)
                .deadLetterRoutingKey(ROUTING_KEY_ORDER_DLQ)
                .build();
    }

    @Bean
    public Queue orderShipmentStatusUpdatedQueue() {
        return QueueBuilder.durable(QUEUE_ORDER_SHIPMENT_STATUS_UPDATED)
                .deadLetterExchange(EXCHANGE_DLX)
                .deadLetterRoutingKey(ROUTING_KEY_ORDER_DLQ)
                .build();
    }

    @Bean
    public Queue orderDeadLetterQueue() {
        return QueueBuilder.durable(QUEUE_ORDER_DLQ).build();
    }

    // Filas de Logística (para garantir topologia completa caso o backend inicie primeiro)
    @Bean
    public Queue shippingOrderCreatedQueue() {
        return QueueBuilder.durable(QUEUE_SHIPPING_ORDER_CREATED)
                .deadLetterExchange(EXCHANGE_DLX)
                .deadLetterRoutingKey(ROUTING_KEY_SHIPPING_DLQ)
                .build();
    }

    @Bean
    public Queue shippingOrderDispatchedQueue() {
        return QueueBuilder.durable(QUEUE_SHIPPING_ORDER_DISPATCHED)
                .deadLetterExchange(EXCHANGE_DLX)
                .deadLetterRoutingKey(ROUTING_KEY_SHIPPING_DLQ)
                .build();
    }

    @Bean
    public Queue shippingOrderCancelledQueue() {
        return QueueBuilder.durable(QUEUE_SHIPPING_ORDER_CANCELLED)
                .deadLetterExchange(EXCHANGE_DLX)
                .deadLetterRoutingKey(ROUTING_KEY_SHIPPING_DLQ)
                .build();
    }

    @Bean
    public Queue shippingDeadLetterQueue() {
        return QueueBuilder.durable(QUEUE_SHIPPING_DLQ).build();
    }

    // ==========================================
    // 3. Bindings (Amarrações de Rotas)
    // ==========================================
    @Bean
    public Binding bindingOrderShipmentCreated(Queue orderShipmentCreatedQueue, TopicExchange shippingExchange) {
        return BindingBuilder.bind(orderShipmentCreatedQueue).to(shippingExchange).with(ROUTING_KEY_SHIPPING_CREATED);
    }

    @Bean
    public Binding bindingOrderShipmentStatusUpdated(Queue orderShipmentStatusUpdatedQueue, TopicExchange shippingExchange) {
        return BindingBuilder.bind(orderShipmentStatusUpdatedQueue).to(shippingExchange).with(ROUTING_KEY_SHIPPING_STATUS_UPDATED);
    }

    @Bean
    public Binding bindingOrderDlq(Queue orderDeadLetterQueue, TopicExchange deadLetterExchange) {
        return BindingBuilder.bind(orderDeadLetterQueue).to(deadLetterExchange).with(ROUTING_KEY_ORDER_DLQ + ".#");
    }

    @Bean
    public Binding bindingOrderDlqDirect(Queue orderDeadLetterQueue, TopicExchange deadLetterExchange) {
        return BindingBuilder.bind(orderDeadLetterQueue).to(deadLetterExchange).with(ROUTING_KEY_ORDER_DLQ);
    }

    @Bean
    public Binding bindingShippingOrderCreated(Queue shippingOrderCreatedQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(shippingOrderCreatedQueue).to(orderExchange).with(ROUTING_KEY_ORDER_CREATED);
    }

    @Bean
    public Binding bindingShippingOrderDispatched(Queue shippingOrderDispatchedQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(shippingOrderDispatchedQueue).to(orderExchange).with(ROUTING_KEY_ORDER_DISPATCHED);
    }

    @Bean
    public Binding bindingShippingOrderCancelled(Queue shippingOrderCancelledQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(shippingOrderCancelledQueue).to(orderExchange).with(ROUTING_KEY_ORDER_CANCELLED);
    }

    @Bean
    public Binding bindingShippingDlq(Queue shippingDeadLetterQueue, TopicExchange deadLetterExchange) {
        return BindingBuilder.bind(shippingDeadLetterQueue).to(deadLetterExchange).with(ROUTING_KEY_SHIPPING_DLQ + ".#");
    }

    @Bean
    public Binding bindingShippingDlqDirect(Queue shippingDeadLetterQueue, TopicExchange deadLetterExchange) {
        return BindingBuilder.bind(shippingDeadLetterQueue).to(deadLetterExchange).with(ROUTING_KEY_SHIPPING_DLQ);
    }

    // ==========================================
    // 4. Serializador JSON e Type Mapping
    // ==========================================
    @Bean
    public MessageConverter jacksonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultClassMapper classMapper = new DefaultClassMapper();
        classMapper.setTrustedPackages("*");
        converter.setClassMapper(classMapper);
        converter.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jacksonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jacksonMessageConverter);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            MessageConverter jacksonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(jacksonMessageConverter);
        factory.setDefaultRequeueRejected(false); // Encaminha para DLQ quando rejeitado após retentativas
        return factory;
    }
}

