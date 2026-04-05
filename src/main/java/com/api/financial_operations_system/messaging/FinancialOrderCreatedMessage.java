package com.api.financial_operations_system.messaging;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record FinancialOrderCreatedMessage(
        UUID orderId,
        UUID companyId,
        BigDecimal amount,
        String type,
        String status,
        Instant occurredAt
) {
}
