package com.api.financial_operations_system.dto.fx;

import java.math.BigDecimal;

public record FxHistoryPointResponse(
        String date,
        BigDecimal rate
) {}
