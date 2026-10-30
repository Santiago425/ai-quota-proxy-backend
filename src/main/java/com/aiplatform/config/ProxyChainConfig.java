package com.aiplatform.config;

import com.aiplatform.proxy.CacheProxyService;
import com.aiplatform.proxy.QuotaProxyService;
import com.aiplatform.proxy.RateLimitProxyService;
import com.aiplatform.repository.UserAccountRepository;
import com.aiplatform.service.AIGenerationService;
import com.aiplatform.service.AuditLogService;
import com.aiplatform.service.ClockService;
import com.aiplatform.service.MockAIGenerationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ProxyChainConfig {

    @Bean
    public AIGenerationService aiGenerationService(UserAccountRepository repository,
                                                   ClockService clock,
                                                   AuditLogService auditLog,
                                                   @Value("${app.mock.latency-ms:1200}") long latencyMs,
                                                   @Value("${app.cache.ttl-minutes:10}") long cacheTtlMinutes) {
        AIGenerationService realService = new MockAIGenerationService(latencyMs);
        AIGenerationService cacheProxy = new CacheProxyService(realService, Duration.ofMinutes(cacheTtlMinutes));
        AIGenerationService quotaProxy = new QuotaProxyService(cacheProxy, repository, clock);
        return new RateLimitProxyService(quotaProxy, repository, clock, auditLog);
    }
}
