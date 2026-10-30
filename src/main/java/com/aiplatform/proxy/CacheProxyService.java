package com.aiplatform.proxy;

import com.aiplatform.dto.GenerationRequest;
import com.aiplatform.dto.GenerationResponse;
import com.aiplatform.service.AIGenerationService;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class CacheProxyService implements AIGenerationService {

    private static final int MAX_ENTRIES = 200;

    private final AIGenerationService next;
    private final Duration ttl;
    private final Map<String, CacheEntry> cache = new LinkedHashMap<>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, CacheEntry> eldest) {
            return size() > MAX_ENTRIES;
        }
    };

    public CacheProxyService(AIGenerationService next, Duration ttl) {
        this.next = next;
        this.ttl = ttl;
    }

    @Override
    public GenerationResponse generate(GenerationRequest request) {
        long start = System.currentTimeMillis();
        String key = normalize(request.prompt());
        CacheEntry hit = find(key);
        if (hit != null) {
            return hit.response().asCachedHit(System.currentTimeMillis() - start).passedThrough("CacheProxyService");
        }
        GenerationResponse response = next.generate(request);
        store(key, response);
        return response.passedThrough("CacheProxyService");
    }

    private synchronized CacheEntry find(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.storedAt().plus(ttl).isBefore(Instant.now())) {
            cache.remove(key);
            return null;
        }
        return entry;
    }

    private synchronized void store(String key, GenerationResponse response) {
        cache.put(key, new CacheEntry(response.asCachedHit(0), Instant.now()));
    }

    private String normalize(String prompt) {
        return prompt.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private record CacheEntry(GenerationResponse response, Instant storedAt) {
    }
}
