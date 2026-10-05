package com.demorgs.rng.service;

import com.demorgs.rng.config.ChiSquareProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Regulatory self-check: verifies that generated integers are uniformly distributed.
 * Keeps one histogram per range (sized to that range) and runs a chi-square test
 * once enough numbers accumulate for a range.
 *
 * Note: chiSquareTest(...) returns true when uniformity is REJECTED,
 * i.e. true means the test FAILED (numbers not uniform).
 */
@Component
public class ChiSquareMonitor {

    private static final Logger log = LoggerFactory.getLogger(ChiSquareMonitor.class);

    static final long MAX_RANGE = 10_000; // wider ranges are not tested: histogram too big, too few hits per bucket
    private final double significance;
    private final long checkInterval;    // draws per range before a test runs

    private final ChiSquareTest chiSquareTest = new ChiSquareTest();
    private final Counter failureCounter;
    private final Counter checkCounter;
    private volatile double lastPValue = Double.NaN; // NaN until the first check runs

    // one histogram + counter per range, keyed by "min:max"
    private final Map<String, long[]> histograms = new HashMap<>();
    private final Map<String, Long> counts = new HashMap<>();

    public ChiSquareMonitor(MeterRegistry registry, ChiSquareProperties properties) {
        this.significance = properties.significance();
        this.checkInterval = properties.checkInterval();
        this.failureCounter = Counter.builder("rng_chisquare_failures")
                .description("Number of chi-square uniformity test failures")
                .register(registry);
        this.checkCounter = Counter.builder("rng_chisquare_checks")
                .description("Number of chi-square uniformity tests run")
                .register(registry);
        Gauge.builder("rng_chisquare_last_pvalue", this, m -> m.lastPValue)
                .description("p-value of the most recent chi-square test (near 0 = suspicious)")
                .register(registry);
    }

    /**
     * Records one generated value into its range's histogram and runs the chi-square
     * test once that range has accumulated rng.chi-square.check-interval draws.
     * synchronized: the singleton is shared across Tomcat request threads.
     *
     * @param value the generated integer to record
     * @param min inclusive lower bound of the range it came from
     * @param max inclusive upper bound of the range it came from
     */
    public synchronized void record(int value, int min, int max) {
        // long math: max - min + 1 overflows int for very wide ranges
        long range = (long) max - min + 1;
        if (range > MAX_RANGE) {
            return;
        }
        int size = (int) range;
        String key = min + ":" + max;

        long[] observed = histograms.computeIfAbsent(key, k -> new long[size]);
        observed[value - min]++; // value - min = 0-based index into the histogram

        long total = counts.merge(key, 1L, Long::sum);

        if (total >= checkInterval) {
            runTest(key, observed, total);
            histograms.put(key, new long[size]); // reset window
            counts.put(key, 0L);
        }
    }

    /** Runs the chi-square test for one range. */
    private void runTest(String key, long[] observed, long total) {
        // chi-square needs at least 2 categories; a single-value range can't be tested
        if (observed.length < 2) {
            return;
        }

        double expectedPerBucket = (double) total / observed.length;
        double[] expected = new double[observed.length];
        Arrays.fill(expected, expectedPerBucket);

        double pValue = chiSquareTest.chiSquareTest(expected, observed);
        lastPValue = pValue;
        checkCounter.increment();

        if (pValue < significance) {
            failureCounter.increment();
            log.warn("RNG chi-square FAILED for range {} over {} draws, p-value {} (not uniform)", key, total, pValue);
        } else {
            log.debug("RNG chi-square passed for range {} over {} draws, p-value {}", key, total, pValue);
        }
    }
}
