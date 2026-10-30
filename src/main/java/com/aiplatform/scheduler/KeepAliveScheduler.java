package com.aiplatform.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class KeepAliveScheduler {

    private static final Logger log = LoggerFactory.getLogger(KeepAliveScheduler.class);

    private final String publicUrl;
    private final RestClient restClient = RestClient.create();

    public KeepAliveScheduler(@Value("${RENDER_EXTERNAL_URL:}") String publicUrl) {
        this.publicUrl = publicUrl;
    }

    @Scheduled(initialDelay = 60_000, fixedDelay = 600_000)
    public void ping() {
        if (publicUrl.isBlank()) {
            return;
        }
        try {
            restClient.get().uri(publicUrl + "/actuator/health").retrieve().toBodilessEntity();
        } catch (Exception e) {
            log.warn("Keep-alive sin respuesta: {}", e.getMessage());
        }
    }
}
