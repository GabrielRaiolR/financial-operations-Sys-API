package com.api.financial_operations_system.service;

import com.api.financial_operations_system.dto.fx.FxHistoryPointResponse;
import com.api.financial_operations_system.dto.fx.FxHistoryResponse;
import com.api.financial_operations_system.dto.fx.FxLatestQuote;
import com.api.financial_operations_system.dto.fx.FxRateResponse;
import com.api.financial_operations_system.integration.fx.FrankfurterExchangeRateClient;
import com.api.financial_operations_system.integration.fx.FrankfurterTimeSeriesResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

@Service
public class FxReferenceService {

    /**
     * Extra calendar days to request before the nominal window so weekends and holidays
     * still yield up to {@code calendarDaysInclusive} daily points (Frankfurter omits non-trading days).
     */
    private static final int HISTORY_CALENDAR_BUFFER_DAYS = 14;

    private static final int REFERENCE_AMOUNT_SCALE = 2;
    private final ExchangeRateProvider exchangeRateProvider;
    private final FrankfurterExchangeRateClient frankfurterExchangeRateClient;

    public FxReferenceService(
            ExchangeRateProvider exchangeRateProvider,
            FrankfurterExchangeRateClient frankfurterExchangeRateClient) {
        this.exchangeRateProvider = exchangeRateProvider;
        this.frankfurterExchangeRateClient = frankfurterExchangeRateClient;
    }

    public FxRateResponse getRateForApi(String from, String to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("from e to são obrigatórios");
        }
        String f = from.trim().toUpperCase();
        String t = to.trim().toUpperCase();
        if (f.isEmpty() || t.isEmpty()) {
            throw new IllegalArgumentException("from e to são obrigatórios");
        }
        FxLatestQuote quote = frankfurterExchangeRateClient.getLatestQuote(f, t);
        Instant asOf = quote.referenceDate().atStartOfDay(ZoneOffset.UTC).toInstant();
        return new FxRateResponse(
                quote.rate(), f, t, asOf, "frankfurter", quote.referenceDate().toString());
    }

    /**
     * Up to {@code calendarDaysInclusive} most recent <strong>trading</strong> days from Frankfurter (UTC dates).
     * <ul>
     *   <li>Non-trading days are omitted by the provider (gaps on the X axis are normal).</li>
     *   <li>A wide calendar window plus trim yields N business days even across weekends.</li>
     *   <li>The time-series endpoint can lag {@code /latest}; we append or refresh the last point via
     *       {@link FrankfurterExchangeRateClient#fetchLatestQuoteForHistoryMerge} (always hits Frankfurter and
     *       updates {@code fxRates}) so the chart matches the spot cards and is not skewed by a stale cache hit
     *       on {@link FrankfurterExchangeRateClient#getLatestQuote}.</li>
     *   <li>The last point is the provider’s reference date, not necessarily “today” in your time zone.</li>
     * </ul>
     */
    public FxHistoryResponse getHistoryForApi(String from, String to, int calendarDaysInclusive) {
        FrankfurterExchangeRateClient.validateHistorySpan(calendarDaysInclusive);
        if (from == null || to == null) {
            throw new IllegalArgumentException("from e to são obrigatórios");
        }
        String f = from.trim().toUpperCase();
        String t = to.trim().toUpperCase();
        if (f.isEmpty() || t.isEmpty()) {
            throw new IllegalArgumentException("from e to são obrigatórios");
        }
        LocalDate end = FrankfurterExchangeRateClient.utcToday();
        long nominalSpan = calendarDaysInclusive - 1L;
        long bufferedSpan = nominalSpan + HISTORY_CALENDAR_BUFFER_DAYS;
        LocalDate start = end.minusDays(bufferedSpan);
        FrankfurterTimeSeriesResponse raw = frankfurterExchangeRateClient.fetchTimeSeries(f, t, start, end);
        List<FxHistoryPointResponse> points = mapTimeSeriesToPoints(raw, t);
        if (points.size() > calendarDaysInclusive) {
            int fromIndex = points.size() - calendarDaysInclusive;
            points = new ArrayList<>(points.subList(fromIndex, points.size()));
        }
        points = alignHistoryWithLatestDailyRate(f, t, points, calendarDaysInclusive);
        return new FxHistoryResponse(f, t, "frankfurter", points);
    }

    /**
     * Time-series sometimes lags {@code /latest}; merge the published daily quote so the last point
     * matches spot and uses the provider's reference date.
     */
    private List<FxHistoryPointResponse> alignHistoryWithLatestDailyRate(
            String from,
            String to,
            List<FxHistoryPointResponse> points,
            int calendarDaysInclusive) {
        FxLatestQuote latest = frankfurterExchangeRateClient.fetchLatestQuoteForHistoryMerge(from, to);
        String latestDay = latest.referenceDate().toString();
        List<FxHistoryPointResponse> out = new ArrayList<>(points);
        if (out.isEmpty()) {
            return List.of(new FxHistoryPointResponse(latestDay, latest.rate()));
        }
        String lastDay = out.get(out.size() - 1).date();
        int cmp = latestDay.compareTo(lastDay);
        if (cmp > 0) {
            out.add(new FxHistoryPointResponse(latestDay, latest.rate()));
        } else if (cmp == 0) {
            out.set(out.size() - 1, new FxHistoryPointResponse(latestDay, latest.rate()));
        }
        if (out.size() > calendarDaysInclusive) {
            return new ArrayList<>(out.subList(out.size() - calendarDaysInclusive, out.size()));
        }
        return out;
    }

    private static List<FxHistoryPointResponse> mapTimeSeriesToPoints(FrankfurterTimeSeriesResponse raw, String quote) {
        Map<String, Map<String, BigDecimal>> rates = raw.rates();
        List<FxHistoryPointResponse> out = new ArrayList<>();
        for (String date : new TreeSet<>(rates.keySet())) {
            Map<String, BigDecimal> day = rates.get(date);
            if (day != null && day.containsKey(quote)) {
                out.add(new FxHistoryPointResponse(date, day.get(quote)));
            }
        }
        return out;
    }

    public BigDecimal computeReferenceAmountUsd(BigDecimal amountBrl) {
        if (amountBrl == null) {
            throw new IllegalArgumentException("amountBrl é obrigatório");
        }
        BigDecimal rate = exchangeRateProvider.getRate("BRL", "USD");
        return amountBrl.multiply(rate).setScale(REFERENCE_AMOUNT_SCALE, RoundingMode.HALF_UP);
    }
}
