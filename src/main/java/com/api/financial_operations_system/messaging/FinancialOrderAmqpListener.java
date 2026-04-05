package com.api.financial_operations_system.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FinancialOrderAmqpListener {

    @RabbitListener(queues = RabbitMqNames.QUEUE_ORDERS)
    public void onCreated(FinancialOrderCreatedMessage message) {
        log.info("Ordem criada (async): {} empresa {} valor {}", message.orderId(), message.companyId(), message.amount());
    }
}
