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
        return ResponseEntity.badRequest().body(errorBody(
                400,
                "Bad Request",
                exception.getMessage()));
    }

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<Map<String, Object>> handleJibbleApiError(
            RestClientResponseException exception) {

        Map<String, Object> body = errorBody(
                502,
                "Jibble API request failed",
                exception.getMessage());

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

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody(
                500,
                "Configuration Error",
                exception.getMessage()));
    }

    private Map<String, Object> errorBody(int status, String error, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status);
        body.put("error", error);
        body.put("message", message == null ? "No additional details were provided." : message);
        return body;
    }
}
