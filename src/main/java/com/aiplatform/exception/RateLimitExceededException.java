package com.aiplatform.exception;

public class RateLimitExceededException extends RuntimeException {

    private final long retryAfterSeconds;

    public RateLimitExceededException(int limit, long retryAfterSeconds) {
        super("Superaste el límite de " + limit + " solicitudes por minuto de tu plan.");
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
