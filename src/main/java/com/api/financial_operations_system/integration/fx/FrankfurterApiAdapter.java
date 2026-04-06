package com.api.financial_operations_system.integration.fx;

import com.api.financial_operations_system.config.FxRestClientConfig;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Component
public class FrankfurterApiAdapter {

    private static final Logger log = LoggerFactory.getLogger(FrankfurterApiAdapter.class);

    private final RestClient frankfurterRestClient;

    public FrankfurterApiAdapter(
            @Qualifier(FxRestClientConfig.FRANKFURTER_REST_CLIENT) RestClient frankfurterRestClient) {
        this.frankfurterRestClient = frankfurterRestClient;
    }

    @Retry(name = "frankfurter")
    @CircuitBreaker(name = "frankfurter", fallbackMethod = "fetchLatestFallback")
    public BigDecimal fetchLatest(String from, String to) {
        FrankfurterLatestResponse body = frankfurterRestClient.get()
                .uri("/latest?from={from}&to={to}", from, to)
                .retrieve()
                .body(FrankfurterLatestResponse.class);
        if (body == null || body.rates() == null || !body.rates().containsKey(to)) {
            throw new IllegalStateException("Resposta FX sem taxa para " + from + "/" + to);
        }
        return body.rates().get(to);
    }

    @SuppressWarnings("unused")
    private BigDecimal fetchLatestFallback(String from, String to, Throwable t) {
        log.warn("Frankfurter indisponível ou circuito aberto: {} / {} — {}", from, to, t.toString());
        throw new IllegalStateException("Cotação indisponível no momento", t);
    }
}
