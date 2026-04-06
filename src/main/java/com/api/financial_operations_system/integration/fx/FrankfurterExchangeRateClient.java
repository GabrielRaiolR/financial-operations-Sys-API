package com.api.financial_operations_system.integration.fx;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;

@Service
public class FrankfurterExchangeRateClient {

    private final FrankfurterApiAdapter frankfurterApiAdapter;

    public FrankfurterExchangeRateClient(FrankfurterApiAdapter frankfurterApiAdapter) {
        this.frankfurterApiAdapter = frankfurterApiAdapter;
    }

    @Cacheable(cacheNames = "fxRates", key = "#from + '-' + #to")
    public BigDecimal getRate(String from, String to) {
        if (from.equalsIgnoreCase(to)) {
            return BigDecimal.ONE;
        }
        try {
            return frankfurterApiAdapter.fetchLatest(from, to);
        } catch (RestClientException ex) {
            throw new IllegalStateException("Falha ao consultar API de câmbio", ex);
        }
    }
}
