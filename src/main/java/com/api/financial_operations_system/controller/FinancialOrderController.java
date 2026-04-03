package com.api.financial_operations_system.controller;

import com.api.financial_operations_system.dto.order.CreateFinancialOrderRequest;
import com.api.financial_operations_system.dto.order.FinancialOrderResponse;
import com.api.financial_operations_system.service.FinancialOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/financial-orders")
@RequiredArgsConstructor
public class FinancialOrderController {

    private final FinancialOrderService financialOrderService;

    @PostMapping
    public ResponseEntity<FinancialOrderResponse> create(@Valid @RequestBody CreateFinancialOrderRequest request) {
        FinancialOrderResponse response = financialOrderService.createFinancialOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FinancialOrderResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(financialOrderService.getById(id));
    }
}
