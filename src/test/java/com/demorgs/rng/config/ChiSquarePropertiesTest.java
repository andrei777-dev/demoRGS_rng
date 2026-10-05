package com.demorgs.rng.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link ChiSquareProperties} validation.
 */
class ChiSquarePropertiesTest {

    @Test
    void validValues_areAccepted() {
        assertDoesNotThrow(() -> new ChiSquareProperties(0.05, 100_000));
    }

    @Test
    void significanceZero_isRejected() {
        // 0 is what Spring binds when the YAML key is missing
        assertThrows(IllegalArgumentException.class, () -> new ChiSquareProperties(0, 100_000));
    }

    @Test
    void checkIntervalZero_isRejected() {
        assertThrows(IllegalArgumentException.class, () -> new ChiSquareProperties(0.05, 0));
    }
}