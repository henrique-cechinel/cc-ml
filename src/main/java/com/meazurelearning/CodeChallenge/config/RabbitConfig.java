package com.meazurelearning.CodeChallenge.config;

import com.meazurelearning.CodeChallenge.constants.RabbitConstants;
import jakarta.annotation.PostConstruct;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.stereotype.Component;

@Component
public class RabbitConfig {
    private AmqpAdmin amqpAdmin;
    private RabbitTemplate rabbitTemplate;

    public RabbitConfig(AmqpAdmin amqpAdmin, RabbitTemplate rabbitTemplate) {
        this.amqpAdmin = amqpAdmin;
        this.rabbitTemplate = rabbitTemplate;
    }

    public Queue queue(String queueName) {
        return new Queue(queueName, true, false, false);
    }

    public DirectExchange exchange(String exchangeName) {
        return new DirectExchange(exchangeName);
    }

    public Binding binding(Queue queueName, DirectExchange exchangeName, String routingKey) {
        return BindingBuilder.bind(queueName).to(exchangeName).with(routingKey);
    }

    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new SimpleMessageConverter());
        return rabbitTemplate;
    }

    @PostConstruct
    public void configureBindings() {
        Queue clientCreatedQueue = queue(RabbitConstants.CLIENT_CREATED_QUEUE);
        Queue clientUpdatedQueue = queue(RabbitConstants.CLIENT_UPDATED_QUEUE);
        Queue clientDeletedQueue = queue(RabbitConstants.CLIENT_DELETED_QUEUE);

        DirectExchange exchange = exchange("amq.direct");

        Binding clientCreatedBinding = binding(clientCreatedQueue, exchange, RabbitConstants.CLIENT_CREATED_ROUTING_KEY);
        Binding clientUpdatedBinding = binding(clientUpdatedQueue, exchange, RabbitConstants.CLIENT_UPDATED_ROUTING_KEY);
        Binding clientDeletedBinding = binding(clientDeletedQueue, exchange, RabbitConstants.CLIENT_DELETED_ROUTING_KEY);

        amqpAdmin.declareQueue(clientCreatedQueue);
        amqpAdmin.declareQueue(clientUpdatedQueue);
        amqpAdmin.declareQueue(clientDeletedQueue);

        amqpAdmin.declareExchange(exchange);

        amqpAdmin.declareBinding(clientCreatedBinding);
        amqpAdmin.declareBinding(clientUpdatedBinding);
        amqpAdmin.declareBinding(clientDeletedBinding);
    }
}
