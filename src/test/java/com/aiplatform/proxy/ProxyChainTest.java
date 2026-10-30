package com.aiplatform.proxy;

import com.aiplatform.dto.GenerationRequest;
import com.aiplatform.dto.GenerationResponse;
import com.aiplatform.exception.QuotaExceededException;
import com.aiplatform.exception.RateLimitExceededException;
import com.aiplatform.model.Plan;
import com.aiplatform.repository.UserAccountRepository;
import com.aiplatform.service.AIGenerationService;
import com.aiplatform.service.AuditLogService;
import com.aiplatform.service.ClockService;
import com.aiplatform.service.MockAIGenerationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProxyChainTest {

    private UserAccountRepository repository;
    private AIGenerationService chain;
    private AuditLogService auditLog;

    @BeforeEach
    void setUp() {
        ClockService clock = new ClockService("America/Bogota");
        repository = new UserAccountRepository(clock);
        auditLog = new AuditLogService(clock);
        AIGenerationService real = new MockAIGenerationService(0);
        AIGenerationService cache = new CacheProxyService(real, Duration.ofMinutes(10));
        chain = new RateLimitProxyService(new QuotaProxyService(cache, repository, clock), repository, clock, auditLog);
    }

    @Test
    void requestPassesThroughWholeChainInOrder() {
        GenerationResponse response = chain.generate(new GenerationRequest("ana", "Hola"));
        assertEquals(List.of("RateLimitProxyService", "QuotaProxyService", "CacheProxyService", "MockAIGenerationService"), response.getChain());
        assertEquals(response.getTotalTokens(), repository.findOrCreate("ana").getTokensUsed());
    }

    @Test
    void repeatedPromptIsServedFromCacheWithoutChargingTokens() {
        GenerationResponse first = chain.generate(new GenerationRequest("sara", "¿Qué es un proxy?"));
        long usedAfterFirst = repository.findOrCreate("sara").getTokensUsed();
        GenerationResponse second = chain.generate(new GenerationRequest("sara", "  ¿qué es un   PROXY? "));
        assertTrue(second.isCached());
        assertEquals(first.getText(), second.getText());
        assertEquals(List.of("RateLimitProxyService", "QuotaProxyService", "CacheProxyService"), second.getChain());
        assertEquals(usedAfterFirst, repository.findOrCreate("sara").getTokensUsed());
    }

    @Test
    void auditLogRecordsEveryOutcome() {
        chain.generate(new GenerationRequest("tom", "Hola"));
        chain.generate(new GenerationRequest("tom", "Hola"));
        assertThrows(QuotaExceededException.class, () -> chain.generate(new GenerationRequest("tom", "b".repeat(200_004))));
        List<String> outcomes = auditLog.latestFor("tom", 10).stream().map(e -> e.outcome()).toList();
        assertEquals(List.of("QUOTA_EXCEEDED", "CACHED", "GENERATED"), outcomes);
    }

    @Test
    void freePlanIsBlockedAfterTenRequestsPerMinute() {
        for (int i = 0; i < 10; i++) {
            chain.generate(new GenerationRequest("luis", "Prompt " + i));
        }
        RateLimitExceededException ex = assertThrows(RateLimitExceededException.class,
                () -> chain.generate(new GenerationRequest("luis", "Uno más")));
        assertTrue(ex.getRetryAfterSeconds() >= 1 && ex.getRetryAfterSeconds() <= 60);
    }

    @Test
    void quotaIsRejectedWhenPromptExceedsRemainingTokens() {
        String hugePrompt = "a".repeat(200_004);
        assertThrows(QuotaExceededException.class, () -> chain.generate(new GenerationRequest("eva", hugePrompt)));
        assertEquals(0, repository.findOrCreate("eva").getTokensUsed());
    }

    @Test
    void enterprisePlanHasNoLimits() {
        repository.findOrCreate("corp").setPlan(Plan.ENTERPRISE);
        for (int i = 0; i < 80; i++) {
            chain.generate(new GenerationRequest("corp", "Prompt " + i));
        }
        assertEquals(80, repository.findOrCreate("corp").getRequestsInWindow());
    }
}
