package com.aiplatform.service;

import com.aiplatform.dto.GenerationRequest;
import com.aiplatform.dto.GenerationResponse;

public interface AIGenerationService {

    GenerationResponse generate(GenerationRequest request);
}
