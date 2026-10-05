package com.demorgs.rng.dto.response;

import java.time.Instant;

/** Standard error body returned by the API. */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
