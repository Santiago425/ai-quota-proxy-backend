package com.aiplatform.dto;

public record PlanResponse(String name, int requestsPerMinute, long monthlyTokens, int priceUsd, boolean unlimited) {
}
