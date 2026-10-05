package com.demorgs.rng.service;

import com.demorgs.rng.dto.request.GenerateDoublesRequest;
import com.demorgs.rng.dto.request.GenerateIntegersRequest;
import com.demorgs.rng.dto.response.GenerateDoublesResponse;
import com.demorgs.rng.dto.response.GenerateIntegersResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link RngService}. Dependencies are mocked with Mockito.
 */
@ExtendWith(MockitoExtension.class)
public class RngServiceTest {

    @Mock
    private SecureRandomGenerator generator;

    @Mock
    private ChiSquareMonitor monitor;

    @InjectMocks
    private RngService rngService;

    @Test
    void generateIntegers_countZero_throws() {
        GenerateIntegersRequest request = new GenerateIntegersRequest(1, 6, 0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> rngService.generateIntegers(request));

        assertEquals(RngService.COUNT_MUST_BE_POSITIVE, ex.getMessage());
        verifyNoInteractions(generator, monitor);
    }

    @Test
    void generateIntegers_minGreaterThanMax_throws() {
        GenerateIntegersRequest request = new GenerateIntegersRequest(9, 1, 3);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> rngService.generateIntegers(request));

        assertEquals(RngService.MIN_MUST_NOT_EXCEED_MAX, ex.getMessage());
        verifyNoInteractions(generator, monitor);
    }

    @Test
    void generateIntegers_validRequest_returnsGeneratedValuesAndRecordsEach() {
        when(generator.nextInt(1, 6)).thenReturn(4, 2, 6);
        GenerateIntegersRequest request = new GenerateIntegersRequest(1, 6, 3);

        GenerateIntegersResponse response = rngService.generateIntegers(request);

        assertEquals(List.of(4, 2, 6), response.values());

        verify(monitor).record(4, 1, 6);
        verify(monitor).record(2, 1, 6);
        verify(monitor).record(6, 1, 6);
        verify(generator, times(3)).nextInt(1, 6);
    }

    @Test
    void generateDoubles_countZero_throws() {
        GenerateDoublesRequest request = new GenerateDoublesRequest(0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> rngService.generateDoubles(request));

        assertEquals(RngService.COUNT_MUST_BE_POSITIVE, ex.getMessage());
        verifyNoInteractions(generator, monitor);
    }

    @Test
    void generateDoubles_validRequest_returnsGeneratedValuesWithoutMonitoring() {
        when(generator.nextDouble()).thenReturn(0.1, 0.5, 0.9);
        GenerateDoublesRequest request = new GenerateDoublesRequest(3);

        GenerateDoublesResponse response = rngService.generateDoubles(request);

        assertEquals(List.of(0.1, 0.5, 0.9), response.values());
        verify(generator, times(3)).nextDouble();
        verifyNoInteractions(monitor);   // doubles are not monitored (see TODO in RngService)
    }

    @Test
    void generateIntegers_countAboveMax_throws() {
        GenerateIntegersRequest request = new GenerateIntegersRequest(1, 6, RngService.MAX_COUNT + 1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> rngService.generateIntegers(request));

        assertEquals(RngService.COUNT_TOO_LARGE, ex.getMessage());
        verifyNoInteractions(generator, monitor);
    }

    @Test
    void generateIntegers_countAtMax_isAccepted() {
        when(generator.nextInt(1, 6)).thenReturn(3);
        GenerateIntegersRequest request = new GenerateIntegersRequest(1, 6, RngService.MAX_COUNT);

        GenerateIntegersResponse response = rngService.generateIntegers(request);

        assertEquals(RngService.MAX_COUNT, response.values().size());
    }

    @Test
    void generateDoubles_countAboveMax_throws() {
        GenerateDoublesRequest request = new GenerateDoublesRequest(RngService.MAX_COUNT + 1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> rngService.generateDoubles(request));

        assertEquals(RngService.COUNT_TOO_LARGE, ex.getMessage());
        verifyNoInteractions(generator, monitor);
    }
}