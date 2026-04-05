package com.api.financial_operations_system.events;

import com.api.financial_operations_system.domain.order.FinancialOrder;

public record FinancialOrderCreatedEvent(FinancialOrder order){
}
