package com.api.financial_operations_system.dto.fx;

import java.math.BigDecimal;
import java.time.Instant;

public record FxRateResponse(
        BigDecimal rate,
        String from,
        String to,
        Instant asOf,
        String source,
        /** Frankfurter {@code date} field, ISO {@code yyyy-MM-dd} (UTC calendar day of the rate). */
        String referenceDate
) {}
