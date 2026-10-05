package com.demorgs.rng.service;

import com.demorgs.rng.config.ChiSquareProperties;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link ChiSquareMonitor}. Real in-memory MeterRegistry, small check interval.
 */
class ChiSquareMonitorTest {

    private static final int CHECK_INTERVAL = 10;

    private SimpleMeterRegistry registry;
    private ChiSquareMonitor monitor;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        monitor = new ChiSquareMonitor(registry, new ChiSquareProperties(0.05, CHECK_INTERVAL));
    }

    private double failures() {
        return registry.get("rng_chisquare_failures").counter().count();
    }

    @Test
    void record_uniformValues_doesNotCountFailure() {
        // range 1..5, each value twice = perfectly uniform [2,2,2,2,2]
        for (int round = 0; round < 2; round++) {
            for (int v = 1; v <= 5; v++) {
                monitor.record(v, 1, 5);
            }
        }

        assertEquals(0, failures());
    }

    @Test
    void record_biasedValues_countsOneFailure() {
        // always 1 → histogram [10,0,0,0,0], clearly not uniform
        for (int i = 0; i < CHECK_INTERVAL; i++) {
            monitor.record(1, 1, 5);
        }

        assertEquals(1, failures());
    }

    @Test
    void record_beforeInterval_doesNotRunTest() {
        // 9 biased draws: not enough yet, so no test runs
        for (int i = 0; i < CHECK_INTERVAL - 1; i++) {
            monitor.record(1, 1, 5);
        }
        assertEquals(0, failures());

        // the 10th draw triggers the test
        monitor.record(1, 1, 5);
        assertEquals(1, failures());
    }

    @Test
    void record_afterTest_resetsHistogram() {
        // window 1: biased → 1 failure
        for (int i = 0; i < CHECK_INTERVAL; i++) {
            monitor.record(1, 1, 5);
        }
        assertEquals(1, failures());

        // window 2: uniform → must pass on its own
        for (int round = 0; round < 2; round++) {
            for (int v = 1; v <= 5; v++) {
                monitor.record(v, 1, 5);
            }
        }
        assertEquals(1, failures());   // still 1: old biased counts were cleared
    }

    @Test
    void record_differentRanges_areCountedSeparately() {
        // 9 draws in each range = 18 total, but no single range reaches 10
        for (int i = 0; i < CHECK_INTERVAL - 1; i++) {
            monitor.record(1, 1, 5);
            monitor.record(1, 1, 6);
        }

        assertEquals(0, failures());   // no test ran, so ranges did not mix
    }

    @Test
    void record_singleValueRange_isSkippedWithoutError() {
        // range 5..5 has 1 category: chi-square is impossible, so it must be skipped
        assertDoesNotThrow(() -> {
            for (int i = 0; i < CHECK_INTERVAL; i++) {
                monitor.record(5, 5, 5);
            }
        });

        assertEquals(0, failures());
    }
}