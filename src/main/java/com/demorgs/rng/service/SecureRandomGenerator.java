package com.demorgs.rng.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Secure source of random numbers, backed by {@link SecureRandom}.
 * Self-seeded from OS entropy (no manual seed), so values are unpredictable.
 */
@Component
public class SecureRandomGenerator {

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Returns a secure random int in [min, max], both inclusive.
     * Works for any min &lt;= max, including the full int range.
     *
     * @param min inclusive lower bound
     * @param max inclusive upper bound
     * @return a uniformly distributed value in [min, max]
     */
    public int nextInt(int min, int max) {
        // long math: max - min + 1 overflows int for very wide ranges
        return (int) secureRandom.nextLong(min, (long) max + 1);
    }

    /** Returns a secure random double in [0, 1). Caller scales it if needed. */
    public double nextDouble() {
        return secureRandom.nextDouble();
    }
}
