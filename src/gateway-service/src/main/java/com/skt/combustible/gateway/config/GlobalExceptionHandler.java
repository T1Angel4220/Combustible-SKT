package com.skt.combustible.gateway.config;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;

import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para asegurar que los headers CORS
 * siempre se incluyan en las respuestas de error
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        logger.warn("Método HTTP no soportado: {} para URI: {}", e.getMethod(), request.getRequestURI());
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", "Método HTTP no soportado: " + e.getMethod());
        errorResponse.put("error", e.getMessage());
        errorResponse.put("type", "METHOD_NOT_SUPPORTED");
        errorResponse.put("supportedMethods", e.getSupportedHttpMethods());
        
        HttpHeaders headers = new HttpHeaders();
        addCorsHeaders(headers, request);
        
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .headers(headers)
                .body(errorResponse);
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<Map<String, Object>> handleResourceAccessException(
            ResourceAccessException e, HttpServletRequest request) {
        logger.error("Error de conexión: {}", e.getMessage(), e);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", "Error de conexión con el servicio backend");
        errorResponse.put("error", e.getMessage());
        errorResponse.put("type", "CONNECTION_ERROR");
        
        HttpHeaders headers = new HttpHeaders();
        addCorsHeaders(headers, request);
        
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .headers(headers)
                .body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception e, HttpServletRequest request) {
        logger.error("Error inesperado: {}", e.getMessage(), e);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", "Error interno del servidor");
        errorResponse.put("error", e.getMessage());
        errorResponse.put("type", "INTERNAL_ERROR");
        errorResponse.put("exception", e.getClass().getSimpleName());
        
        HttpHeaders headers = new HttpHeaders();
        addCorsHeaders(headers, request);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .headers(headers)
                .body(errorResponse);
    }
    
    private void addCorsHeaders(HttpHeaders headers, HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        if (origin != null && !origin.isEmpty()) {
            headers.add("Access-Control-Allow-Origin", origin);
            headers.add("Access-Control-Allow-Credentials", "true");
        } else {
            headers.add("Access-Control-Allow-Origin", "*");
        }
        headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH, HEAD");
        headers.add("Access-Control-Allow-Headers", "*");
        headers.add("Access-Control-Expose-Headers", "*");
    }
}

