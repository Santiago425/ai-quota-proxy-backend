package com.aiplatform.config;

import com.aiplatform.proxy.QuotaProxyService;
import com.aiplatform.proxy.RateLimitProxyService;
import com.aiplatform.repository.UserAccountRepository;
import com.aiplatform.service.AIGenerationService;
import com.aiplatform.service.ClockService;
import com.aiplatform.service.MockAIGenerationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProxyChainConfig {

    @Bean
    public AIGenerationService aiGenerationService(UserAccountRepository repository,
                                                   ClockService clock,
                                                   @Value("${app.mock.latency-ms:1200}") long latencyMs) {
        AIGenerationService realService = new MockAIGenerationService(latencyMs);
        AIGenerationService quotaProxy = new QuotaProxyService(realService, repository, clock);
        return new RateLimitProxyService(quotaProxy, repository, clock);
    }
}
