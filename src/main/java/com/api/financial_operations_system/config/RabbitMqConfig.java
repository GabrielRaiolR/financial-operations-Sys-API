package com.api.financial_operations_system.config;

import com.api.financial_operations_system.messaging.RabbitMqNames;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    DirectExchange finopsExchange() {
        return new DirectExchange(RabbitMqNames.EXCHANGE, true, false);
    }

    @Bean
    Queue financialOrderQueue() {
        return new Queue(RabbitMqNames.QUEUE_ORDERS, true);
    }

    @Bean
    Binding financialOrderBinding(Queue financialOrderQueue, DirectExchange finopsExchange) {
        return BindingBuilder.bind(financialOrderQueue)
                .to(finopsExchange)
                .with(RabbitMqNames.RK_ORDER_CREATED);
    }

    @Bean
    Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory cf, Jackson2JsonMessageConverter conv) {
        RabbitTemplate t = new RabbitTemplate(cf);
        t.setMessageConverter(conv);
        return t;
    }

}
