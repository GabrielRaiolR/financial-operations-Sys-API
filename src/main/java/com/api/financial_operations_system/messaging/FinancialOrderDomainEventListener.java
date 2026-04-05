package com.api.financial_operations_system.messaging;

import com.api.financial_operations_system.events.FinancialOrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class FinancialOrderDomainEventListener {

    private final FinancialOrderEventPublisher publisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCreated(FinancialOrderCreatedEvent event) {
        publisher.publishCreated(event.order());
    }

}
