package com.aiplatform.scheduler;

import com.aiplatform.repository.UserAccountRepository;
import com.aiplatform.service.ClockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class QuotaResetScheduler {

    private static final Logger log = LoggerFactory.getLogger(QuotaResetScheduler.class);

    private final UserAccountRepository repository;
    private final ClockService clock;

    public QuotaResetScheduler(UserAccountRepository repository, ClockService clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Scheduled(cron = "0 * * * * *", zone = "${app.zone:America/Bogota}")
    public void resetRateLimits() {
        repository.findAll().forEach(account -> account.resetWindow());
    }

    @Scheduled(cron = "0 0 0 1 * *", zone = "${app.zone:America/Bogota}")
    public void resetMonthlyQuotas() {
        repository.findAll().forEach(account -> account.resetMonthlyQuota(clock.nextMonthlyReset()));
        log.info("Cuotas mensuales reiniciadas para {} usuarios", repository.findAll().size());
    }
}
