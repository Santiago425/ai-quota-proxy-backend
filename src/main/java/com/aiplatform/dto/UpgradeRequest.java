package com.aiplatform.dto;

import jakarta.validation.constraints.NotBlank;

public record UpgradeRequest(@NotBlank String userId, String targetPlan, String paymentReference) {
}
