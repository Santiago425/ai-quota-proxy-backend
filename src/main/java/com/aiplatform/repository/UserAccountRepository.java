package com.aiplatform.repository;

import com.aiplatform.model.Plan;
import com.aiplatform.model.UserAccount;
import com.aiplatform.service.ClockService;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class UserAccountRepository {

    private final Map<String, UserAccount> accounts = new ConcurrentHashMap<>();
    private final ClockService clock;

    public UserAccountRepository(ClockService clock) {
        this.clock = clock;
    }

    public UserAccount findOrCreate(String userId) {
        return accounts.computeIfAbsent(userId.trim().toLowerCase(),
                id -> new UserAccount(id, Plan.FREE, clock.nextMonthlyReset()));
    }

    public Collection<UserAccount> findAll() {
        return accounts.values();
    }
}
