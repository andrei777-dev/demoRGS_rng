package com.demorgs.rng.controller;

import com.demorgs.rng.dto.response.GenerateDoublesResponse;
import com.demorgs.rng.dto.request.GenerateDoublesRequest;
import com.demorgs.rng.dto.request.GenerateIntegersRequest;
import com.demorgs.rng.dto.response.GenerateIntegersResponse;
import com.demorgs.rng.service.RngService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST entry point for the RNG service.
 * Exposes secure random number generation to internal callers (game engines such as scratch-engine).
 * Delegates all logic to {@link RngService}.
 */
@Tag(name = "RNG", description = "Endpoints for generating random numbers.")
@RestController
@RequestMapping("/api/v1/rng")
public class RngController {

    private final RngService rngService;

    public RngController(RngService rngService) {
        this.rngService = rngService;
    }

    /**
     * Generates a list of secure random integers within an inclusive range.
     *
     * @param request the range bounds and how many integers to generate (min, max, count)
     * @return the generated integers wrapped in a response object
     */
    @Operation(
            summary = "Generate random integers",
            description = "Returns 'count' secure random integers, each in the inclusive range [min, max]."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Integers generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input (count <= 0, count > 10000 or min > max)")
    })
    @PostMapping("/integers")
    public GenerateIntegersResponse generateIntegers(@RequestBody GenerateIntegersRequest request) {
        return rngService.generateIntegers(request);
    }

    /**
     * Generates a list of secure random doubles, each in the range [0, 1).
     *
     * @param request how many doubles to generate (count)
     * @return the generated doubles wrapped in a response object
     */
    @Operation(
            summary = "Generate random doubles",
            description = "Returns 'count' secure random doubles, each in the range [0, 1)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Doubles generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input (count <= 0 or count > 10000)")
    })
    @PostMapping("/doubles")
    public GenerateDoublesResponse generateDoubles(@RequestBody GenerateDoublesRequest request) {
        return rngService.generateDoubles(request);
    }

}
