package com.aiplatform.dto;

public record ErrorResponse(int status, String error, String message, Long retryAfterSeconds) {
}
