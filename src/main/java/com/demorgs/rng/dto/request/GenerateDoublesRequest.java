package com.demorgs.rng.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

/** Request to generate doubles: how many to produce. */
public record GenerateDoublesRequest(
        @Schema(description = "How many doubles to generate", example = "5",
                requiredMode = Schema.RequiredMode.REQUIRED) int count
) {}
