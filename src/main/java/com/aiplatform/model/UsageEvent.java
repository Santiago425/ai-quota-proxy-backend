package com.aiplatform.model;

import java.time.ZonedDateTime;

public record UsageEvent(
        ZonedDateTime timestamp,
        String userId,
        String promptPreview,
        int status,
        String outcome,
        String stoppedAt,
        long chargedTokens,
        long latencyMs
) {
}
