package com.aiplatform.model;

public enum Plan {
    FREE(10, 50_000L, 0),
    PRO(60, 500_000L, 19),
    ENTERPRISE(-1, -1L, 99);

    private final int requestsPerMinute;
    private final long monthlyTokens;
    private final int priceUsd;

    Plan(int requestsPerMinute, long monthlyTokens, int priceUsd) {
        this.requestsPerMinute = requestsPerMinute;
        this.monthlyTokens = monthlyTokens;
        this.priceUsd = priceUsd;
    }

    public int getRequestsPerMinute() {
        return requestsPerMinute;
    }

    public long getMonthlyTokens() {
        return monthlyTokens;
    }

    public int getPriceUsd() {
        return priceUsd;
    }

    public boolean isUnlimited() {
        return this == ENTERPRISE;
    }

    public boolean canUpgradeTo(Plan target) {
        return target.ordinal() > this.ordinal();
    }
}
