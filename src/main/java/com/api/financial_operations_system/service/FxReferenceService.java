package com.api.financial_operations_system.service;

import com.api.financial_operations_system.dto.fx.FxRateResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Service
public class FxReferenceService {

    private static final int REFERENCE_AMOUNT_SCALE = 2;
    private final ExchangeRateProvider exchangeRateProvider;

    public FxReferenceService(ExchangeRateProvider exchangeRateProvider) {
        this.exchangeRateProvider = exchangeRateProvider;
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
        BigDecimal rate = exchangeRateProvider.getRate(f, t);
        return new FxRateResponse(rate, f, t, Instant.now(), "frankfurter");
    }

    public BigDecimal computeReferenceAmountUsd(BigDecimal amountBrl) {
        if (amountBrl == null) {
            throw new IllegalArgumentException("amountBrl é obrigatório");
        }
        BigDecimal rate = exchangeRateProvider.getRate("BRL", "USD");
        return amountBrl.multiply(rate).setScale(REFERENCE_AMOUNT_SCALE, RoundingMode.HALF_UP);
    }
}
