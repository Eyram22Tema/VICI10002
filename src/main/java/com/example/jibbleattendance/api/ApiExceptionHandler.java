package com.example.jibbleattendance.api;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "error", "Bad Request",
                "message", exception.getMessage()));
    }

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<Map<String, Object>> handleJibbleApiError(
            RestClientResponseException exception) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 502);
        body.put("error", "Jibble API request failed");
        body.put("jibbleStatus", exception.getStatusCode().value());

        String responseBody = exception.getResponseBodyAsString();
        if (responseBody != null && responseBody.length() > 1500) {
            responseBody = responseBody.substring(0, 1500) + "...";
        }
        body.put("jibbleResponse", responseBody);

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleConfigurationError(
            IllegalStateException exception) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", 500,
                "error", "Configuration Error",
                "message", exception.getMessage()));
    }
}
