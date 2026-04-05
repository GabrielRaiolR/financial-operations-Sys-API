package com.api.financial_operations_system.service;

import java.math.BigDecimal;

public interface ExchangeRateProvider {
    BigDecimal getRate(String fromCurrency, String toCurrency);
}
