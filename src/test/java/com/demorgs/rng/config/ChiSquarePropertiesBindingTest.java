package com.demorgs.rng.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Checks that rng.chi-square.* is loaded from application.yaml into ChiSquareProperties.
 */
@SpringBootTest
class ChiSquarePropertiesBindingTest {

    @Autowired
    private ChiSquareProperties properties;

    @Test
    void valuesAreLoadedFromYaml() {
        assertNotNull(properties);
        assertTrue(properties.significance() > 0, "significance not loaded from YAML");
        assertTrue(properties.checkInterval() > 0, "check-interval not loaded from YAML");
    }
}