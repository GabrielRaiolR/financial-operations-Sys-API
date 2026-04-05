package com.api.financial_operations_system.messaging;

import com.api.financial_operations_system.domain.order.FinancialOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class FinancialOrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishCreated(FinancialOrder order) {
        var msg = new FinancialOrderCreatedMessage(
                order.getId(),
                order.getCompany().getId(),
                order.getAmount(),
                order.getOrderType().name(),
                order.getOrderStatus().name(),
                Instant.now());
        rabbitTemplate.convertAndSend(
                RabbitMqNames.EXCHANGE,
                RabbitMqNames.RK_ORDER_CREATED,
                msg
        );
    }
}
