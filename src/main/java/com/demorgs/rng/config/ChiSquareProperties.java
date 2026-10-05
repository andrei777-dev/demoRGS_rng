package com.demorgs.rng.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Chi-square monitor settings, bound from {@code rng.chi-square.*} in application.yaml. */
@ConfigurationProperties(prefix = "rng.chi-square")
public record ChiSquareProperties(double significance, long checkInterval) {

    /** Fails fast at startup instead of breaking requests later. */
    public ChiSquareProperties {
        // commons-math accepts alpha only in (0, 0.5]
        if (significance <= 0 || significance > 0.5) {
            throw new IllegalArgumentException(
                    "rng.chi-square.significance must be in (0, 0.5], got " + significance);
        }
        if (checkInterval <= 0) {
            throw new IllegalArgumentException(
                    "rng.chi-square.check-interval must be > 0, got " + checkInterval);
        }
    }
}