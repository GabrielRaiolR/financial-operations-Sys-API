package com.api.financial_operations_system.controller;

import com.api.financial_operations_system.dto.fx.FxHistoryResponse;
import com.api.financial_operations_system.dto.fx.FxRateResponse;
import com.api.financial_operations_system.service.FxReferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fx")
@RequiredArgsConstructor
public class FxController {

    private final FxReferenceService fxReferenceService;

    @GetMapping("/rate")
    public FxRateResponse getRate(@RequestParam String from, @RequestParam String to) {
        return fxReferenceService.getRateForApi(from, to);
    }

    /**
     * Daily closing rates over a UTC calendar window (default 5 days, inclusive of today).
     */
    @GetMapping("/history")
    public FxHistoryResponse getHistory(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam(defaultValue = "5") int days) {
        return fxReferenceService.getHistoryForApi(from, to, days);
    }
}
