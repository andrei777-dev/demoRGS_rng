package com.demorgs.rng.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link SecureRandomGenerator}. No Spring, no mocks.
 */
class SecureRandomGeneratorTest {

    @Test
    void nextInt_smallRange_staysWithinBounds() {
        SecureRandomGenerator generator = new SecureRandomGenerator();
        int min = 1;
        int max = 3;

        // Act + Assert (random output: one call proves nothing, so repeat many times)
        for (int i = 0; i < 10_000; i++) {
            int value = generator.nextInt(min, max);
            assertTrue(value >= min && value <= max, "value " + value + " is outside [" + min + ", " + max + "]");
        }
    }

    @Test
    void nextInt_smallRange_hitsEveryValue() {
        SecureRandomGenerator generator = new SecureRandomGenerator();
        int min = 1;
        int max = 3;
        boolean[] seen = new boolean[max - min + 1]; // [false, false, false]

        for (int i = 0; i < 10_000; i++) {
            seen[generator.nextInt(min, max) - min] = true;
        }

        for (int i = min; i <= max; i++) {
            assertTrue(seen[i - min], "value " + i + " was never generated");
        }
    }

    @Test
    void nextInt_minEqualsMax_alwaysReturnsThatValue() {
        SecureRandomGenerator generator = new SecureRandomGenerator();

        for (int i = 0; i < 100; i++) {
            assertEquals(5, generator.nextInt(5, 5));
        }
    }

    @Test
    void nextDouble_alwaysInZeroToOneExclusive() {
        SecureRandomGenerator generator = new SecureRandomGenerator();

        for (int i = 0; i < 10_000; i++) {
            double value = generator.nextDouble();
            assertTrue(value >= 0.0 && value < 1.0, "value " + value + " is outside [0, 1)");
        }
    }

    @Test
    void nextInt_negativeRange_staysWithinBounds() {
        SecureRandomGenerator generator = new SecureRandomGenerator();
        int min = -5;
        int max = 5;

        for (int i = 0; i < 10_000; i++) {
            int value = generator.nextInt(min, max);
            assertTrue(value >= min && value <= max, "value " + value + " is outside [" + min + ", " + max + "]");
        }
    }

    @Test
    void nextInt_fullIntRange_doesNotOverflow() {
        SecureRandomGenerator generator = new SecureRandomGenerator();

        // regression: max - min + 1 used to overflow int; generator now uses long math
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 1_000; i++) {
                generator.nextInt(Integer.MIN_VALUE, Integer.MAX_VALUE);
            }
        });
    }
}