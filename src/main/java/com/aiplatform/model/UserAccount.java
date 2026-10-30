package com.aiplatform.model;

import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;

public class UserAccount {

    private final String userId;
    private Plan plan;
    private long tokensUsed;
    private int requestsInWindow;
    private LocalDate resetDate;
    private final Map<LocalDate, DailyUsage> dailyUsage = new TreeMap<>();

    public UserAccount(String userId, Plan plan, LocalDate resetDate) {
        this.userId = userId;
        this.plan = plan;
        this.resetDate = resetDate;
    }

    public synchronized boolean tryAcquireRequest() {
        if (plan.isUnlimited()) {
            requestsInWindow++;
            return true;
        }
        if (requestsInWindow >= plan.getRequestsPerMinute()) {
            return false;
        }
        requestsInWindow++;
        return true;
    }

    public synchronized long remainingTokens() {
        if (plan.isUnlimited()) {
            return -1;
        }
        return Math.max(0, plan.getMonthlyTokens() - tokensUsed);
    }

    public synchronized boolean hasTokensFor(long tokens) {
        return plan.isUnlimited() || tokensUsed + tokens <= plan.getMonthlyTokens();
    }

    public synchronized void consume(long tokens, LocalDate day) {
        tokensUsed += tokens;
        dailyUsage.computeIfAbsent(day, DailyUsage::new).add(tokens);
    }

    public synchronized void resetWindow() {
        requestsInWindow = 0;
    }

    public synchronized void resetMonthlyQuota(LocalDate nextResetDate) {
        tokensUsed = 0;
        resetDate = nextResetDate;
    }

    public synchronized DailyUsage usageOn(LocalDate day) {
        DailyUsage usage = dailyUsage.get(day);
        return usage == null ? new DailyUsage(day) : usage.copy();
    }

    public String getUserId() {
        return userId;
    }

    public synchronized Plan getPlan() {
        return plan;
    }

    public synchronized void setPlan(Plan plan) {
        this.plan = plan;
    }

    public synchronized long getTokensUsed() {
        return tokensUsed;
    }

    public synchronized int getRequestsInWindow() {
        return requestsInWindow;
    }

    public synchronized LocalDate getResetDate() {
        return resetDate;
    }
}
