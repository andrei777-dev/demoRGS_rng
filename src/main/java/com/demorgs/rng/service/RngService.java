package com.demorgs.rng.service;

import com.demorgs.rng.dto.request.GenerateDoublesRequest;
import com.demorgs.rng.dto.request.GenerateIntegersRequest;
import com.demorgs.rng.dto.response.GenerateDoublesResponse;
import com.demorgs.rng.dto.response.GenerateIntegersResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Core RNG logic: orchestrates number generation and feeds the uniformity monitor.
 * Uses {@link SecureRandomGenerator} to produce values
 * and {@link ChiSquareMonitor} to check their distribution.
 */
@Service
public class RngService {

    static final int MAX_COUNT = 10_000;
    static final String COUNT_TOO_LARGE = "count must be <= " + MAX_COUNT;
    static final String COUNT_MUST_BE_POSITIVE = "count must be > 0";
    static final String MIN_MUST_NOT_EXCEED_MAX = "min must be <= max";

    private final SecureRandomGenerator generator;
    private final ChiSquareMonitor monitor;

    public RngService(SecureRandomGenerator generator, ChiSquareMonitor monitor) {
        this.generator = generator;
        this.monitor = monitor;
    }

    /**
     * Generates a list of secure random integers within an inclusive range.
     *
     * @param request the range bounds and how many integers to generate (min, max, count)
     * @return the generated integers wrapped in a response object
     * @throws IllegalArgumentException if count &lt;= 0, count &gt; 10000 or min &gt; max
     */
    public GenerateIntegersResponse generateIntegers(GenerateIntegersRequest request) {
        validateCount(request.count());
        if (request.min() > request.max()) {
            throw new IllegalArgumentException(MIN_MUST_NOT_EXCEED_MAX);
        }

        List<Integer> values = new ArrayList<>();

        for (int i = 0; i < request.count(); i++) {
            int v = generator.nextInt(request.min(), request.max());
            monitor.record(v, request.min(), request.max());
            values.add(v);
        }

        return new GenerateIntegersResponse(values);
    }

    /**
     * Generates a list of secure random doubles, each in the range [0, 1).
     *
     * @param request how many doubles to generate (count)
     * @return the generated doubles wrapped in a response object
     * @throws IllegalArgumentException if count &lt;= 0, count &gt; 10000
     */
    public GenerateDoublesResponse generateDoubles(GenerateDoublesRequest request) {
        validateCount(request.count());

        List<Double> values = new ArrayList<>();

        // TODO: doubles not monitored directly. Covered indirectly via integers
        // (same SecureRandom source). For direct check: bucket [0,1) into N slots + chi-square.
        for (int i = 0; i < request.count(); i++) {
            values.add(generator.nextDouble());
        }

        return new GenerateDoublesResponse(values);
    }

    /** Rejects non-positive counts and counts large enough to exhaust memory. */
    private void validateCount(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException(COUNT_MUST_BE_POSITIVE);
        }
        if (count > MAX_COUNT) {
            throw new IllegalArgumentException(COUNT_TOO_LARGE);
        }
    }
}
