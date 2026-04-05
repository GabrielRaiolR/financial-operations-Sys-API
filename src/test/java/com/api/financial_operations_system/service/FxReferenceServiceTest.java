package com.api.financial_operations_system.service;

import com.api.financial_operations_system.dto.fx.FxRateResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FxReferenceServiceTest {

    @Mock
    private ExchangeRateProvider exchangeRateProvider;

    @InjectMocks
    private FxReferenceService fxReferenceService;

    @Test
    void getRateForApi_normalizesAndReturnsResponse() {
        when(exchangeRateProvider.getRate("BRL", "USD")).thenReturn(new BigDecimal("0.19"));

        FxRateResponse response = fxReferenceService.getRateForApi(" brl ", " usd ");

        assertThat(response.from()).isEqualTo("BRL");
        assertThat(response.to()).isEqualTo("USD");
        assertThat(response.rate()).isEqualByComparingTo("0.19");
        assertThat(response.source()).isEqualTo("frankfurter");
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
}
