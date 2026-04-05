package com.api.financial_operations_system.integration.fx;

import com.api.financial_operations_system.config.FxRestClientConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;

@Service
public class FrankfurterExchangeRateClient {

    private final RestClient frankfurterRestClient;

    public FrankfurterExchangeRateClient(
            @Qualifier(FxRestClientConfig.FRANKFURTER_REST_CLIENT) RestClient frankfurterRestClient) {
        this.frankfurterRestClient = frankfurterRestClient;
    }

    @Cacheable(cacheNames = "fxRates", key = "#from + '-' + #to")
    public BigDecimal getRate(String from, String to) {
        if (from.equalsIgnoreCase(to)) {
            return BigDecimal.ONE;
        }
        try {
            FrankfurterLatestResponse body = frankfurterRestClient.get()
                    .uri("/latest?from={from}&to={to}", from, to)
                    .retrieve()
                    .body(FrankfurterLatestResponse.class);
            if (body == null || body.rates() == null || !body.rates().containsKey(to)) {
                throw new IllegalStateException("Resposta FX sem taxa para " + from + "/" + to);
            }
            return body.rates().get(to);
        } catch (RestClientException ex) {
            throw new IllegalStateException("Falha ao consultar API de câmbio", ex);
        }
    }
}
