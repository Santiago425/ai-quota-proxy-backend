package com.aiplatform.proxy;

import com.aiplatform.dto.GenerationRequest;
import com.aiplatform.dto.GenerationResponse;
import com.aiplatform.exception.QuotaExceededException;
import com.aiplatform.exception.RateLimitExceededException;
import com.aiplatform.model.UserAccount;
import com.aiplatform.repository.UserAccountRepository;
import com.aiplatform.service.AIGenerationService;
import com.aiplatform.service.AuditLogService;
import com.aiplatform.service.ClockService;

public class RateLimitProxyService implements AIGenerationService {

    private final AIGenerationService next;
    private final UserAccountRepository repository;
    private final ClockService clock;
    private final AuditLogService auditLog;

    public RateLimitProxyService(AIGenerationService next, UserAccountRepository repository, ClockService clock, AuditLogService auditLog) {
        this.next = next;
        this.repository = repository;
        this.clock = clock;
        this.auditLog = auditLog;
    }

    @Override
    public GenerationResponse generate(GenerationRequest request) {
        long start = System.currentTimeMillis();
        UserAccount account = repository.findOrCreate(request.userId());
        if (!account.tryAcquireRequest()) {
            auditLog.record(request.userId(), request.prompt(), 429, "RATE_LIMITED", "RateLimitProxyService", 0, elapsed(start));
            throw new RateLimitExceededException(account.getPlan().getRequestsPerMinute(), clock.secondsUntilNextMinute());
        }
        try {
            GenerationResponse response = next.generate(request);
            String outcome = response.isCached() ? "CACHED" : "GENERATED";
            String servedBy = response.isCached() ? "CacheProxyService" : "MockAIGenerationService";
            auditLog.record(request.userId(), request.prompt(), 200, outcome, servedBy, response.getChargedTokens(), elapsed(start));
            return response.passedThrough("RateLimitProxyService");
        } catch (QuotaExceededException e) {
            auditLog.record(request.userId(), request.prompt(), 402, "QUOTA_EXCEEDED", "QuotaProxyService", 0, elapsed(start));
            throw e;
        }
    }

    private long elapsed(long start) {
        return System.currentTimeMillis() - start;
    }
}
