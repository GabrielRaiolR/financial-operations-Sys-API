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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class FrankfurterApiAdapter {

    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    private static final Logger log = LoggerFactory.getLogger(FrankfurterApiAdapter.class);

    private final RestClient frankfurterRestClient;

    public FrankfurterApiAdapter(
            @Qualifier(FxRestClientConfig.FRANKFURTER_REST_CLIENT) RestClient frankfurterRestClient) {
        this.frankfurterRestClient = frankfurterRestClient;
    }

    @Retry(name = "frankfurter")
    @CircuitBreaker(name = "frankfurter", fallbackMethod = "fetchLatestResponseFallback")
    public FrankfurterLatestResponse fetchLatestResponse(String from, String to) {
        FrankfurterLatestResponse body = frankfurterRestClient.get()
                .uri("/latest?from={from}&to={to}", from, to)
                .retrieve()
                .body(FrankfurterLatestResponse.class);
        if (body == null || body.rates() == null || !body.rates().containsKey(to)) {
            throw new IllegalStateException("Resposta FX sem taxa para " + from + "/" + to);
        }
        if (body.date() == null || body.date().isBlank()) {
            throw new IllegalStateException("Resposta FX sem data de referência para " + from + "/" + to);
        }
        return body;
    }

    public BigDecimal fetchLatest(String from, String to) {
        return fetchLatestResponse(from, to).rates().get(to);
    }

    /**
     * Frankfurter v1 time series (base URL já inclui {@code /v1}):
     * {@code GET {base}/{start}..{end}?base=USD&symbols=BRL}
     */
    @Retry(name = "frankfurter")
    @CircuitBreaker(name = "frankfurter", fallbackMethod = "fetchTimeSeriesFallback")
    public FrankfurterTimeSeriesResponse fetchTimeSeries(LocalDate start, LocalDate end, String base, String symbol) {
        String startStr = start.format(ISO_DATE);
        String endStr = end.format(ISO_DATE);
        FrankfurterTimeSeriesResponse body = frankfurterRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/{start}..{end}")
                        .queryParam("base", base)
                        .queryParam("symbols", symbol)
                        .build(startStr, endStr))
                .retrieve()
                .body(FrankfurterTimeSeriesResponse.class);
        if (body == null || body.rates() == null || body.rates().isEmpty()) {
            throw new IllegalStateException("Resposta FX time-series vazia para " + base + "/" + symbol);
        }
        return body;
    }

    @SuppressWarnings("unused")
    private FrankfurterTimeSeriesResponse fetchTimeSeriesFallback(
            LocalDate start, LocalDate end, String base, String symbol, Throwable t) {
        log.warn("Frankfurter time-series indisponível: {} / {} — {}", base, symbol, t.toString());
        throw new IllegalStateException("Histórico de câmbio indisponível no momento", t);
    }

    @SuppressWarnings("unused")
    private FrankfurterLatestResponse fetchLatestResponseFallback(String from, String to, Throwable t) {
        log.warn("Frankfurter indisponível ou circuito aberto: {} / {} — {}", from, to, t.toString());
        throw new IllegalStateException("Cotação indisponível no momento", t);
    }
}
