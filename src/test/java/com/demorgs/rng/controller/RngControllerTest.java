package com.demorgs.rng.controller;

import com.demorgs.rng.dto.request.GenerateIntegersRequest;
import com.demorgs.rng.dto.response.GenerateDoublesResponse;
import com.demorgs.rng.dto.response.GenerateIntegersResponse;
import com.demorgs.rng.service.RngService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer tests for {@link RngController}: HTTP mapping, JSON, status codes and error format.
 * RngService is mocked; no server is started.
 */
@WebMvcTest(RngController.class)
class RngControllerTest {

    private static final String BASE = "/api/v1/rng";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RngService rngService;

    @Test
    void generateIntegers_validRequest_returns200WithValues() throws Exception {
        when(rngService.generateIntegers(any(GenerateIntegersRequest.class)))
                .thenReturn(new GenerateIntegersResponse(List.of(4, 2, 6)));

        mockMvc.perform(post(BASE + "/integers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"min\":1,\"max\":6,\"count\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.values.length()").value(3))
                .andExpect(jsonPath("$.values[0]").value(4));
    }

    @Test
    void generateIntegers_serviceRejectsInput_returns400ApiError() throws Exception {
        when(rngService.generateIntegers(any(GenerateIntegersRequest.class)))
                .thenThrow(new IllegalArgumentException("count must be > 0"));

        mockMvc.perform(post(BASE + "/integers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"min\":1,\"max\":6,\"count\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("count must be > 0"))
                .andExpect(jsonPath("$.path").value(BASE + "/integers"));
    }

    @Test
    void generateDoubles_validRequest_returns200WithValues() throws Exception {
        when(rngService.generateDoubles(any()))
                .thenReturn(new GenerateDoublesResponse(List.of(0.1, 0.5)));

        mockMvc.perform(post(BASE + "/doubles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"count\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.values.length()").value(2));
    }

    @Test
    void brokenJson_returns400() throws Exception {
        mockMvc.perform(post(BASE + "/integers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"min\":1,"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getInsteadOfPost_returns405() throws Exception {
        mockMvc.perform(get(BASE + "/integers"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
    }

    @Test
    void xmlContentType_returns415() throws Exception {
        mockMvc.perform(post(BASE + "/integers")
                        .contentType(MediaType.APPLICATION_XML)
                        .content("<a/>"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415));
    }
}