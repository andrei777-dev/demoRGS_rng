package com.demorgs.rng.dto.response;

import java.util.List;

/**
 * Response body for POST /api/v1/rng/integers.
 */
public record GenerateIntegersResponse(List<Integer> values) {
}
