package com.aiplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GenerationRequest(
        @NotBlank @Size(max = 64) String userId,
        @NotBlank @Size(max = 400_000) String prompt
) {
}
