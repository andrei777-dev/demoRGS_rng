package com.demorgs.rng.exception;

import com.demorgs.rng.dto.response.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;

/**
 * Translates exceptions thrown by controllers into clean HTTP error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles invalid input (e.g. count &lt;= 0, count &gt; 10000, min &gt; max) by returning HTTP 400.
     *
     * @param ex the exception carrying the validation message
     * @param request the current HTTP request, used to include its path in the response
     * @return a 400 Bad Request with an ApiError body
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex,
                                                          HttpServletRequest request) {
        log.warn("Bad Request on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError body = new ApiError(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Catches any unexpected error, logs it with the full stack trace and returns HTTP 500.
     *
     * @param ex the unexpected exception
     * @param request the current HTTP request, used to include its path in the response
     * @return a 500 Internal Server Error with an ApiError body
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex,
                                                     HttpServletRequest request) {
        log.error("Unexpected error on {}", request.getRequestURI(), ex);
        ApiError body = new ApiError(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred",
                request.getRequestURI()
        );
        return ResponseEntity.internalServerError().body(body);
    }

    /**
     * Formats Spring's built-in MVC errors (malformed JSON, unknown URL, wrong HTTP method,
     * unsupported content type) as ApiError, keeping the status code Spring chose.
     *
     * @param ex the Spring MVC exception
     * @param body the body Spring prepared (replaced by an ApiError)
     * @param headers response headers to keep
     * @param statusCode the status Spring chose (400, 404, 405, 415...)
     * @param request the current request, used to include its path in the response
     * @return a response with the original status and an ApiError body
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body,
                                                             HttpHeaders headers,
                                                             HttpStatusCode statusCode,
                                                             WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        log.warn("Client error {} on {}: {}", statusCode.value(), path, ex.getMessage());

        ApiError apiError = new ApiError(
                Instant.now(),
                statusCode.value(),
                HttpStatus.valueOf(statusCode.value()).getReasonPhrase(),
                ex.getMessage(),
                path
        );
        return new ResponseEntity<>(apiError, headers, statusCode);
    }
}
