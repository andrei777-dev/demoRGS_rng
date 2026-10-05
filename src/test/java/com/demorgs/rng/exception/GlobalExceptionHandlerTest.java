package com.demorgs.rng.exception;

import com.demorgs.rng.dto.response.ApiError;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for {@link GlobalExceptionHandler}. Handler methods are called directly, no HTTP.
 */
class GlobalExceptionHandlerTest {

    private static final String PATH = "/api/v1/rng/integers";

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleIllegalArgument_returns400WithMessageAndPath() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", PATH);
        IllegalArgumentException ex = new IllegalArgumentException("count must be > 0");

        ResponseEntity<ApiError> response = handler.handleIllegalArgument(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiError body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.status());
        assertEquals("Bad Request", body.error());
        assertEquals("count must be > 0", body.message());
        assertEquals(PATH, body.path());
        assertNotNull(body.timestamp());
    }

    @Test
    void handleUnexpected_returns500WithoutLeakingDetails() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", PATH);
        RuntimeException ex = new RuntimeException("secret internal detail: db password=xyz");

        ResponseEntity<ApiError> response = handler.handleUnexpected(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ApiError body = response.getBody();
        assertNotNull(body);
        assertEquals(500, body.status());
        assertEquals("Internal Server Error", body.error());
        assertEquals("An unexpected error occurred", body.message());
        assertFalse(body.message().contains("secret"));   // internal message must never reach the client
        assertEquals(PATH, body.path());
        assertNotNull(body.timestamp());
    }

    @Test
    void handleExceptionInternal_keepsSpringStatusAndUsesApiErrorFormat() {
        // simulates a Spring MVC error (e.g. unknown URL → 404) passing through the overridden hook
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/rng/xyz");
        RuntimeException ex = new RuntimeException("No static resource api/v1/rng/xyz");

        ResponseEntity<Object> response = handler.handleExceptionInternal(
                ex, null, new HttpHeaders(), HttpStatus.NOT_FOUND, new ServletWebRequest(request));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ApiError body = assertInstanceOf(ApiError.class, response.getBody());
        assertEquals(404, body.status());
        assertEquals("Not Found", body.error());
        assertEquals("/api/v1/rng/xyz", body.path());
        assertNotNull(body.timestamp());
    }
}
