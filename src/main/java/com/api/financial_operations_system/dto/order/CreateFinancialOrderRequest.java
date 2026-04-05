package com.api.financial_operations_system.dto.order;

import com.api.financial_operations_system.domain.order.OrderType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;


public record CreateFinancialOrderRequest(
        @NotNull
        OrderType type,

        @NotNull
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        BigDecimal amount,

        @NotBlank
        String description
) {
}
