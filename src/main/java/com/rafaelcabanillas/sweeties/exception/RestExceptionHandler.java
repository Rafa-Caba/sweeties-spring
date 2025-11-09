package com.rafaelcabanillas.sweeties.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class RestExceptionHandler {

    private Map<String, Object> error(String error, String message) {
        Map<String, Object> m = new HashMap<>();
        m.put("error", error);
        m.put("message", message);
        return m;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleInvalid(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fields.put(fe.getField(), fe.getDefaultMessage());
        }
        log.warn("Validation error: {}", fields);
        Map<String, Object> body = error("Validation failed", "One or more fields are invalid");
        body.put("fields", fields);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraint(ConstraintViolationException ex) {
        log.warn("Constraint violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(error("Constraint violation", ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleBadBody(HttpMessageNotReadableException ex) {
        log.warn("Request body parse error: {}", ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(error("Malformed request", "Cannot parse 'item' JSON or multipart payload"));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleRSE(ResponseStatusException ex) {
        String msg = ex.getReason() != null ? ex.getReason() : ex.getMessage();
        log.warn("ResponseStatusException {}: {}", ex.getStatusCode(), msg);
        return ResponseEntity.status(ex.getStatusCode())
                .body(error(ex.getStatusCode().toString(), msg));
    }

    // Spring 6+ “typed” error response exceptions
    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<Map<String, Object>> handleERE(ErrorResponseException ex) {
        var status = ex.getStatusCode();
        var msg = ex.getBody() != null && ex.getBody().getDetail() != null
                ? ex.getBody().getDetail()
                : ex.getMessage();
        log.warn("ErrorResponseException {}: {}", status, msg);
        return ResponseEntity.status(status)
                .body(error(status.toString(), msg));
    }

    // Your domain 404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        log.warn("Not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(error("Not Found", ex.getMessage()));
    }

    // Last-resort catch-all: log full stack and surface root cause text
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAny(HttpServletRequest req, Exception ex) {
        Throwable root = ex;
        while (root.getCause() != null && root.getCause() != root) root = root.getCause();
        String msg = root.getMessage() != null ? root.getMessage() : ex.toString();
        log.error("Unhandled exception on {} {} -> {}", req.getMethod(), req.getRequestURI(), msg, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error("Internal Server Error", msg));
    }
}
