package com.demorgs.rng.dto.response;

import java.util.List;

/** Response holding the generated doubles. */
public record GenerateDoublesResponse(List<Double> values) {
}
