package com.aiplatform.service;

import com.aiplatform.dto.DailyUsageResponse;
import com.aiplatform.dto.PlanResponse;
import com.aiplatform.dto.QuotaStatusResponse;
import com.aiplatform.exception.InvalidUpgradeException;
import com.aiplatform.model.DailyUsage;
import com.aiplatform.model.Plan;
import com.aiplatform.model.UserAccount;
import com.aiplatform.repository.UserAccountRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class QuotaService {

    private final UserAccountRepository repository;
    private final ClockService clock;

    public QuotaService(UserAccountRepository repository, ClockService clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public QuotaStatusResponse status(String userId) {
        UserAccount account = repository.findOrCreate(userId);
        Plan plan = account.getPlan();
        return new QuotaStatusResponse(
                account.getUserId(),
                plan.name(),
                account.getTokensUsed(),
                account.remainingTokens(),
                plan.getMonthlyTokens(),
                plan.isUnlimited(),
                account.getResetDate(),
                account.getRequestsInWindow(),
                plan.getRequestsPerMinute(),
                clock.secondsUntilNextMinute()
        );
    }

    public List<DailyUsageResponse> history(String userId) {
        UserAccount account = repository.findOrCreate(userId);
        LocalDate today = clock.today();
        List<DailyUsageResponse> days = new ArrayList<>();
        for (int offset = 6; offset >= 0; offset--) {
            DailyUsage usage = account.usageOn(today.minusDays(offset));
            days.add(new DailyUsageResponse(usage.getDate(), usage.getTokens(), usage.getRequests()));
        }
        return days;
    }

    public QuotaStatusResponse upgrade(String userId, String targetPlan) {
        UserAccount account = repository.findOrCreate(userId);
        Plan current = account.getPlan();
        Plan target = resolveTarget(current, targetPlan);
        if (!current.canUpgradeTo(target)) {
            throw new InvalidUpgradeException("No es posible pasar de " + current + " a " + target + ".");
        }
        account.setPlan(target);
        return status(userId);
    }

    public List<PlanResponse> plans() {
        return Arrays.stream(Plan.values())
                .map(p -> new PlanResponse(p.name(), p.getRequestsPerMinute(), p.getMonthlyTokens(), p.getPriceUsd(), p.isUnlimited()))
                .toList();
    }

    private Plan resolveTarget(Plan current, String targetPlan) {
        if (targetPlan == null || targetPlan.isBlank()) {
            return current == Plan.FREE ? Plan.PRO : Plan.ENTERPRISE;
        }
        try {
            return Plan.valueOf(targetPlan.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidUpgradeException("El plan " + targetPlan + " no existe.");
        }
    }
}
