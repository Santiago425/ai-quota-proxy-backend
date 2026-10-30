package com.aiplatform.proxy;

import com.aiplatform.dto.GenerationRequest;
import com.aiplatform.dto.GenerationResponse;
import com.aiplatform.exception.QuotaExceededException;
import com.aiplatform.model.UserAccount;
import com.aiplatform.repository.UserAccountRepository;
import com.aiplatform.service.AIGenerationService;
import com.aiplatform.service.ClockService;
import com.aiplatform.service.TokenEstimator;

public class QuotaProxyService implements AIGenerationService {

    private final AIGenerationService next;
    private final UserAccountRepository repository;
    private final ClockService clock;

    public QuotaProxyService(AIGenerationService next, UserAccountRepository repository, ClockService clock) {
        this.next = next;
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public GenerationResponse generate(GenerationRequest request) {
        UserAccount account = repository.findOrCreate(request.userId());
        long requiredTokens = Math.max(1, TokenEstimator.estimate(request.prompt()));
        if (!account.hasTokensFor(requiredTokens)) {
            throw new QuotaExceededException(account.remainingTokens(), requiredTokens);
        }
        GenerationResponse response = next.generate(request);
        account.consume(response.getTotalTokens(), clock.today());
        return response.passedThrough("QuotaProxyService");
    }
}
