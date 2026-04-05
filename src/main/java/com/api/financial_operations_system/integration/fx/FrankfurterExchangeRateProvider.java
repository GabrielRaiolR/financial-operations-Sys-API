package com.api.financial_operations_system.integration.fx;

import com.api.financial_operations_system.service.ExchangeRateProvider;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class FrankfurterExchangeRateProvider implements ExchangeRateProvider {
    private final FrankfurterExchangeRateClient client;
    public FrankfurterExchangeRateProvider(FrankfurterExchangeRateClient client) {
        this.client = client;
    }
    @Override
    public BigDecimal getRate(String fromCurrency, String toCurrency) {
        return client.getRate(fromCurrency, toCurrency);
    }
}
