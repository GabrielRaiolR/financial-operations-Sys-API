package com.api.financial_operations_system.integration.fx;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FrankfurterLatestResponse(
        String base,
        String date,
        Map<String, BigDecimal> rates
) {}
