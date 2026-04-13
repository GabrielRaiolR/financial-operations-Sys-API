package com.api.financial_operations_system.integration.fx;

import com.api.financial_operations_system.dto.fx.FxLatestQuote;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
public class FrankfurterExchangeRateClient {

    private static final int HISTORY_MAX_DAYS = 31;

    private final FrankfurterApiAdapter frankfurterApiAdapter;

    public FrankfurterExchangeRateClient(FrankfurterApiAdapter frankfurterApiAdapter) {
        this.frankfurterApiAdapter = frankfurterApiAdapter;
    }

    /**
     * Daily rates from Frankfurter v1 time-series API (UTC calendar days).
     * <p><strong>Not cached</strong>: Caffeine keyed only by {@code start}/{@code end} kept the same body for the
     * whole UTC day while Frankfurter could update series during the day; that made charts look “stuck” behind
     * {@code /latest}. {@link com.api.financial_operations_system.service.FxReferenceService} still merges the
     * latest daily quote after each fetch; fresh time-series avoids stale gaps.
     */
    public FrankfurterTimeSeriesResponse fetchTimeSeries(String from, String to, LocalDate start, LocalDate end) {
        if (from.equalsIgnoreCase(to)) {
            throw new IllegalArgumentException("from e to devem ser moedas distintas");
        }
        try {
            return frankfurterApiAdapter.fetchTimeSeries(start, end, from, to);
        } catch (RestClientException ex) {
            throw new IllegalStateException("Falha ao consultar histórico FX", ex);
        }
    }

    public static LocalDate utcToday() {
        return LocalDate.now(ZoneOffset.UTC);
    }

    public static void validateHistorySpan(int calendarDaysInclusive) {
        if (calendarDaysInclusive < 1 || calendarDaysInclusive > HISTORY_MAX_DAYS) {
            throw new IllegalArgumentException("days deve estar entre 1 e " + HISTORY_MAX_DAYS);
        }
    }

    /**
     * Latest rate and the provider's reference calendar date (cached per pair).
     */
    @Cacheable(cacheNames = "fxRates", key = "#from + '-' + #to")
    public FxLatestQuote getLatestQuote(String from, String to) {
        return fetchLatestUncached(from, to);
    }

    /**
     * Always calls Frankfurter {@code /latest} and refreshes {@code fxRates} for this pair.
     * Used when merging FX history so the chart’s last point matches spot even if {@link #getLatestQuote}
     * would return a stale cache entry (e.g. parallel {@code GET /fx/rate} vs {@code GET /fx/history}).
     */
    @CachePut(cacheNames = "fxRates", key = "#from + '-' + #to")
    public FxLatestQuote fetchLatestQuoteForHistoryMerge(String from, String to) {
        return fetchLatestUncached(from, to);
    }

    private FxLatestQuote fetchLatestUncached(String from, String to) {
        if (from.equalsIgnoreCase(to)) {
            return new FxLatestQuote(BigDecimal.ONE, utcToday());
        }
        try {
            FrankfurterLatestResponse body = frankfurterApiAdapter.fetchLatestResponse(from, to);
            return new FxLatestQuote(body.rates().get(to), LocalDate.parse(body.date()));
        } catch (RestClientException ex) {
            throw new IllegalStateException("Falha ao consultar API de câmbio", ex);
        }
    }

    public BigDecimal getRate(String from, String to) {
        return getLatestQuote(from, to).rate();
    }
}
