package com.aiplatform.dto;

import java.time.LocalDate;

public record QuotaStatusResponse(
        String userId,
        String plan,
        long tokensUsed,
        long tokensRemaining,
        long tokenLimit,
        boolean unlimited,
        LocalDate resetDate,
        int requestsUsed,
        int requestLimit,
        long secondsToRateReset
) {
}
