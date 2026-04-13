package com.api.financial_operations_system.dto.fx;

import java.util.List;

public record FxHistoryResponse(
        String from,
        String to,
        String source,
        List<FxHistoryPointResponse> points
) {}
