package com.api.financial_operations_system.dto.fx;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Spot rate from the FX provider with the provider's reference calendar date (UTC). */
public record FxLatestQuote(BigDecimal rate, LocalDate referenceDate) {}
