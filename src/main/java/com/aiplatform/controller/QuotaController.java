package com.aiplatform.controller;

import com.aiplatform.dto.DailyUsageResponse;
import com.aiplatform.dto.PlanResponse;
import com.aiplatform.dto.QuotaStatusResponse;
import com.aiplatform.dto.UpgradeRequest;
import com.aiplatform.service.QuotaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/quota")
public class QuotaController {

    private final QuotaService quotaService;

    public QuotaController(QuotaService quotaService) {
        this.quotaService = quotaService;
    }

    @GetMapping("/status")
    public QuotaStatusResponse status(@RequestParam String userId) {
        return quotaService.status(userId);
    }

    @GetMapping("/history")
    public List<DailyUsageResponse> history(@RequestParam String userId) {
        return quotaService.history(userId);
    }

    @PostMapping("/upgrade")
    public QuotaStatusResponse upgrade(@Valid @RequestBody UpgradeRequest request) {
        return quotaService.upgrade(request.userId(), request.targetPlan());
    }

    @GetMapping("/plans")
    public List<PlanResponse> plans() {
        return quotaService.plans();
    }
}
