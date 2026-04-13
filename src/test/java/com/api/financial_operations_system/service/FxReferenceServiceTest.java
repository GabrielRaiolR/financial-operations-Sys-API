package com.api.financial_operations_system.service;

import com.api.financial_operations_system.dto.fx.FxHistoryResponse;
import com.api.financial_operations_system.dto.fx.FxLatestQuote;
import com.api.financial_operations_system.dto.fx.FxRateResponse;
import com.api.financial_operations_system.integration.fx.FrankfurterExchangeRateClient;
import com.api.financial_operations_system.integration.fx.FrankfurterTimeSeriesResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FxReferenceServiceTest {

    @Mock
    private ExchangeRateProvider exchangeRateProvider;

    @Mock
    private FrankfurterExchangeRateClient frankfurterExchangeRateClient;

    @InjectMocks
    private FxReferenceService fxReferenceService;

    @Test
    void getRateForApi_normalizesAndReturnsResponse() {
        when(frankfurterExchangeRateClient.getLatestQuote("BRL", "USD"))
                .thenReturn(new FxLatestQuote(new BigDecimal("0.19"), LocalDate.of(2026, 4, 10)));

        FxRateResponse response = fxReferenceService.getRateForApi(" brl ", " usd ");

        assertThat(response.from()).isEqualTo("BRL");
        assertThat(response.to()).isEqualTo("USD");
        assertThat(response.rate()).isEqualByComparingTo("0.19");
        assertThat(response.source()).isEqualTo("frankfurter");
        assertThat(response.referenceDate()).isEqualTo("2026-04-10");
        assertThat(response.asOf()).isNotNull();
    }

    @Test
    void getRateForApi_rejectsBlankFrom() {
        assertThatThrownBy(() -> fxReferenceService.getRateForApi("  ", "USD"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void computeReferenceAmountUsd_appliesScaleAndRounding() {
        when(exchangeRateProvider.getRate("BRL", "USD")).thenReturn(new BigDecimal("0.192345"));

        BigDecimal result = fxReferenceService.computeReferenceAmountUsd(new BigDecimal("100"));

        assertThat(result).isEqualByComparingTo(new BigDecimal("19.23"));
    }

    @Test
    void getHistoryForApi_mapsSortedPoints() {
        FrankfurterTimeSeriesResponse raw = new FrankfurterTimeSeriesResponse(
                "USD",
                "2026-04-06",
                "2026-04-10",
                Map.of(
                        "2026-04-08",
                        Map.of("BRL", new BigDecimal("5.50")),
                        "2026-04-10",
                        Map.of("BRL", new BigDecimal("5.60"))));
        when(frankfurterExchangeRateClient.fetchTimeSeries(eq("USD"), eq("BRL"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(raw);
        when(frankfurterExchangeRateClient.fetchLatestQuoteForHistoryMerge("USD", "BRL"))
                .thenReturn(new FxLatestQuote(new BigDecimal("5.60"), LocalDate.of(2026, 4, 10)));

        FxHistoryResponse response = fxReferenceService.getHistoryForApi("USD", "BRL", 5);

        assertThat(response.from()).isEqualTo("USD");
        assertThat(response.to()).isEqualTo("BRL");
        assertThat(response.points()).hasSize(2);
        assertThat(response.points().getFirst().date()).isEqualTo("2026-04-08");
        assertThat(response.points().getLast().rate()).isEqualByComparingTo("5.60");
    }

    @Test
    void getHistoryForApi_trimsToLastNTradingDaysWhenBufferReturnsMore() {
        FrankfurterTimeSeriesResponse raw = new FrankfurterTimeSeriesResponse(
                "USD",
                "2026-04-01",
                "2026-04-12",
                Map.of(
                        "2026-04-01",
                        Map.of("BRL", new BigDecimal("5.10")),
                        "2026-04-02",
                        Map.of("BRL", new BigDecimal("5.11")),
                        "2026-04-03",
                        Map.of("BRL", new BigDecimal("5.12")),
                        "2026-04-04",
                        Map.of("BRL", new BigDecimal("5.13")),
                        "2026-04-07",
                        Map.of("BRL", new BigDecimal("5.14")),
                        "2026-04-08",
                        Map.of("BRL", new BigDecimal("5.15")),
                        "2026-04-09",
                        Map.of("BRL", new BigDecimal("5.16"))));
        when(frankfurterExchangeRateClient.fetchTimeSeries(eq("USD"), eq("BRL"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(raw);
        when(frankfurterExchangeRateClient.fetchLatestQuoteForHistoryMerge("USD", "BRL"))
                .thenReturn(new FxLatestQuote(new BigDecimal("5.16"), LocalDate.of(2026, 4, 9)));

        FxHistoryResponse response = fxReferenceService.getHistoryForApi("USD", "BRL", 5);

        assertThat(response.points()).hasSize(5);
        assertThat(response.points().getFirst().date()).isEqualTo("2026-04-03");
        assertThat(response.points().getLast().date()).isEqualTo("2026-04-09");
        assertThat(response.points().getLast().rate()).isEqualByComparingTo("5.16");
    }

    @Test
    void getHistoryForApi_alignsLastPointWhenTimeSeriesEndsBeforeLatest() {
        FrankfurterTimeSeriesResponse raw = new FrankfurterTimeSeriesResponse(
                "USD",
                "2026-04-01",
                "2026-04-12",
                Map.of(
                        "2026-04-01",
                        Map.of("BRL", new BigDecimal("5.18")),
                        "2026-04-06",
                        Map.of("BRL", new BigDecimal("5.08")),
                        "2026-04-07",
                        Map.of("BRL", new BigDecimal("5.09")),
                        "2026-04-08",
                        Map.of("BRL", new BigDecimal("5.10")),
                        "2026-04-09",
                        Map.of("BRL", new BigDecimal("5.07"))));
        when(frankfurterExchangeRateClient.fetchTimeSeries(eq("USD"), eq("BRL"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(raw);
        when(frankfurterExchangeRateClient.fetchLatestQuoteForHistoryMerge("USD", "BRL"))
                .thenReturn(new FxLatestQuote(new BigDecimal("5.0543"), LocalDate.of(2026, 4, 10)));

        FxHistoryResponse response = fxReferenceService.getHistoryForApi("USD", "BRL", 5);

        assertThat(response.points()).hasSize(5);
        assertThat(response.points().getLast().date()).isEqualTo("2026-04-10");
        assertThat(response.points().getLast().rate()).isEqualByComparingTo("5.0543");
        assertThat(response.points().getFirst().date()).isEqualTo("2026-04-06");
    }
}
