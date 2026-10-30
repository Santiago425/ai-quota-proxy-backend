package com.aiplatform.model;

import java.time.LocalDate;

public class DailyUsage {

    private final LocalDate date;
    private long tokens;
    private int requests;

    public DailyUsage(LocalDate date) {
        this.date = date;
    }

    public void add(long tokens) {
        this.tokens += tokens;
        this.requests++;
    }

    public DailyUsage copy() {
        DailyUsage copy = new DailyUsage(date);
        copy.tokens = tokens;
        copy.requests = requests;
        return copy;
    }

    public LocalDate getDate() {
        return date;
    }

    public long getTokens() {
        return tokens;
    }

    public int getRequests() {
        return requests;
    }
}
