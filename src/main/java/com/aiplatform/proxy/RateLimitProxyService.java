package com.aiplatform.proxy;

import com.aiplatform.dto.GenerationRequest;
import com.aiplatform.dto.GenerationResponse;
import com.aiplatform.exception.RateLimitExceededException;
import com.aiplatform.model.UserAccount;
import com.aiplatform.repository.UserAccountRepository;
import com.aiplatform.service.AIGenerationService;
import com.aiplatform.service.ClockService;

public class RateLimitProxyService implements AIGenerationService {

    private final AIGenerationService next;
    private final UserAccountRepository repository;
    private final ClockService clock;

    public RateLimitProxyService(AIGenerationService next, UserAccountRepository repository, ClockService clock) {
        this.next = next;
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public GenerationResponse generate(GenerationRequest request) {
        UserAccount account = repository.findOrCreate(request.userId());
        if (!account.tryAcquireRequest()) {
            throw new RateLimitExceededException(account.getPlan().getRequestsPerMinute(), clock.secondsUntilNextMinute());
        }
        return next.generate(request).passedThrough("RateLimitProxyService");
    }
}
