package com.aiplatform.proxy;

import com.aiplatform.dto.GenerationRequest;
import com.aiplatform.dto.GenerationResponse;
import com.aiplatform.exception.QuotaExceededException;
import com.aiplatform.exception.RateLimitExceededException;
import com.aiplatform.model.Plan;
import com.aiplatform.repository.UserAccountRepository;
import com.aiplatform.service.AIGenerationService;
import com.aiplatform.service.ClockService;
import com.aiplatform.service.MockAIGenerationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProxyChainTest {

    private UserAccountRepository repository;
    private AIGenerationService chain;

    @BeforeEach
    void setUp() {
        ClockService clock = new ClockService("America/Bogota");
        repository = new UserAccountRepository(clock);
        AIGenerationService real = new MockAIGenerationService(0);
        chain = new RateLimitProxyService(new QuotaProxyService(real, repository, clock), repository, clock);
    }

    @Test
    void requestPassesThroughWholeChainInOrder() {
        GenerationResponse response = chain.generate(new GenerationRequest("ana", "Hola"));
        assertEquals(List.of("RateLimitProxyService", "QuotaProxyService", "MockAIGenerationService"), response.getChain());
        assertEquals(response.getTotalTokens(), repository.findOrCreate("ana").getTokensUsed());
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
