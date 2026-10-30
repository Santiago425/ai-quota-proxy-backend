package com.aiplatform.service;

public final class TokenEstimator {

    private static final double CHARS_PER_TOKEN = 4.0;

    private TokenEstimator() {
    }

    public static long estimate(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return (long) Math.ceil(text.trim().length() / CHARS_PER_TOKEN);
    }
}
