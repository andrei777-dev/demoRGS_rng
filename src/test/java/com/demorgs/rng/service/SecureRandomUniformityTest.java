package com.demorgs.rng.service;

import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Statistical tests: real SecureRandomGenerator, 1M draws, chi-square goodness-of-fit.
 * Alpha 0.001 (not 0.05) so a fair RNG fails only ~0.1% of runs instead of ~5% (avoids flaky builds).
 */
@Tag("statistical")
class SecureRandomUniformityTest {

    private static final int DRAWS = 1_000_000;
    private static final double ALPHA = 0.001;

    private final SecureRandomGenerator generator = new SecureRandomGenerator();
    private final ChiSquareTest chiSquareTest = new ChiSquareTest();

    @Test
    void nextInt_dieRange_isUniform() {
        assertUniform(1, 6);
    }

    @Test
    void nextInt_tenValues_isUniform() {
        assertUniform(0, 9);
    }

    @Test
    void nextInt_hundredValues_isUniform() {
        assertUniform(1, 100);
    }

    @Test
    void nextDouble_tenBuckets_isUniform() {
        // split [0, 1) into 10 equal buckets: 0.0–0.1, 0.1–0.2, ...
        int buckets = 10;
        long[] observed = new long[buckets];
        for (int i = 0; i < DRAWS; i++) {
            observed[(int) (generator.nextDouble() * buckets)]++;
        }

        assertPValueAboveAlpha(observed);
    }

    private void assertUniform(int min, int max) {
        long[] observed = new long[max - min + 1];
        for (int i = 0; i < DRAWS; i++) {
            observed[generator.nextInt(min, max) - min]++;
        }

        assertPValueAboveAlpha(observed);
    }

    private void assertPValueAboveAlpha(long[] observed) {
        double[] expected = new double[observed.length];
        Arrays.fill(expected, (double) DRAWS / observed.length);

        double pValue = chiSquareTest.chiSquareTest(expected, observed);
        assertTrue(pValue > ALPHA,
                "not uniform: p-value " + pValue + " <= " + ALPHA + ", histogram " + Arrays.toString(observed));
    }
}