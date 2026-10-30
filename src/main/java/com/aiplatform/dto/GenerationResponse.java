package com.aiplatform.dto;

import java.util.ArrayList;
import java.util.List;

public class GenerationResponse {

    private final String text;
    private final long promptTokens;
    private final long completionTokens;
    private final long latencyMs;
    private final boolean cached;
    private final List<String> chain = new ArrayList<>();

    public GenerationResponse(String text, long promptTokens, long completionTokens, long latencyMs) {
        this(text, promptTokens, completionTokens, latencyMs, false);
    }

    private GenerationResponse(String text, long promptTokens, long completionTokens, long latencyMs, boolean cached) {
        this.text = text;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.latencyMs = latencyMs;
        this.cached = cached;
    }

    public GenerationResponse asCachedHit(long latencyMs) {
        return new GenerationResponse(text, promptTokens, completionTokens, latencyMs, true);
    }

    public GenerationResponse passedThrough(String layer) {
        chain.add(0, layer);
        return this;
    }

    public String getText() {
        return text;
    }

    public long getPromptTokens() {
        return promptTokens;
    }

    public long getCompletionTokens() {
        return completionTokens;
    }

    public long getTotalTokens() {
        return promptTokens + completionTokens;
    }

    public long getChargedTokens() {
        return cached ? 0 : getTotalTokens();
    }

    public long getLatencyMs() {
        return latencyMs;
    }

    public boolean isCached() {
        return cached;
    }

    public List<String> getChain() {
        return chain;
    }
}
