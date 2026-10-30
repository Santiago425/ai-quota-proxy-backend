package com.aiplatform.service;

import com.aiplatform.model.UsageEvent;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

@Service
public class AuditLogService {

    private static final int MAX_EVENTS = 1000;
    private static final int PREVIEW_LENGTH = 60;

    private final Deque<UsageEvent> events = new ArrayDeque<>();
    private final ClockService clock;

    public AuditLogService(ClockService clock) {
        this.clock = clock;
    }

    public void record(String userId, String prompt, int status, String outcome, String stoppedAt, long chargedTokens, long latencyMs) {
        UsageEvent event = new UsageEvent(clock.now(), userId.trim().toLowerCase(), preview(prompt), status, outcome, stoppedAt, chargedTokens, latencyMs);
        synchronized (events) {
            events.addFirst(event);
            if (events.size() > MAX_EVENTS) {
                events.removeLast();
            }
        }
    }

    public List<UsageEvent> latestFor(String userId, int limit) {
        String id = userId.trim().toLowerCase();
        synchronized (events) {
            return events.stream().filter(e -> e.userId().equals(id)).limit(limit).toList();
        }
    }

    private String preview(String prompt) {
        String clean = prompt.trim().replaceAll("\\s+", " ");
        return clean.length() <= PREVIEW_LENGTH ? clean : clean.substring(0, PREVIEW_LENGTH) + "…";
    }
}
