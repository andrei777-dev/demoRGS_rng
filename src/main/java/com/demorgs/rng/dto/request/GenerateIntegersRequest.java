package com.demorgs.rng.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request body for POST /api/v1/rng/integers.
 * min/max are inclusive bounds; count is how many integers to generate.
 */
public record GenerateIntegersRequest(
        @Schema(description = "Inclusive lower bound", example = "0",
                requiredMode = Schema.RequiredMode.REQUIRED) int min,
        @Schema(description = "Inclusive upper bound", example = "9",
                requiredMode = Schema.RequiredMode.REQUIRED) int max,
        @Schema(description = "How many integers to generate", example = "10",
                requiredMode = Schema.RequiredMode.REQUIRED) int count
) {}
