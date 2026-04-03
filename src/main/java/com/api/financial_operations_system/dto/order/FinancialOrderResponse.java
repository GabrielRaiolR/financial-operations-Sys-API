package com.api.financial_operations_system.dto.order;

import com.api.financial_operations_system.domain.order.OrderStatus;
import com.api.financial_operations_system.domain.order.OrderType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record FinancialOrderResponse(
        UUID id,
        UUID companyId,
        BigDecimal amount,
        OrderType type,
        OrderStatus status,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
