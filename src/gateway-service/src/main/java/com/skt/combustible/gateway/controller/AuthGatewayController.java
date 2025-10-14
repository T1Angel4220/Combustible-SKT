package com.skt.combustible.gateway.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para Auth en el Gateway Service
 * Expone endpoints REST que internamente se comunican con auth-service via HTTP
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
public class AuthGatewayController {

    private static final Logger logger = LoggerFactory.getLogger(AuthGatewayController.class);

    /**
     * Endpoint raíz del servicio de auth
     * GET /api/v1/auth
     */
    @GetMapping("")
    public ResponseEntity<Map<String, Object>> getAuthRoot() {
        logger.info("Gateway REST: Acceso al endpoint raíz de auth");

        Map<String, Object> response = new HashMap<>();
        response.put("service", "Auth Gateway Service");
        response.put("version", "1.0.0");
        response.put("description", "Gateway para comunicación con Auth Service");
        response.put("status", "READY");
        response.put("endpoints", new String[] {
                "/api/v1/auth/health",
                "/api/v1/auth/info",
                "/api/v1/auth/login",
                "/api/v1/auth/register"
        });

        return ResponseEntity.ok(response);
    }

    /**
     * Health check del servicio de auth
     * GET /api/v1/auth/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        logger.info("Gateway REST: Health check auth service");
        return ResponseEntity.ok("Auth Gateway Service is running");
    }

    /**
     * Información del servicio de auth
     * GET /api/v1/auth/info
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getAuthInfo() {
        logger.info("Gateway REST: Obteniendo información del auth service");

        Map<String, Object> info = new HashMap<>();
        info.put("service", "Auth Gateway Service");
        info.put("version", "1.0.0");
        info.put("description", "Gateway para comunicación con Auth Service via HTTP");
        info.put("httpEndpoint", "localhost:8085");
        info.put("status", "READY");

        return ResponseEntity.ok(info);
    }

    /**
     * Login de usuario
     * POST /api/v1/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        logger.info("Gateway REST: Login de usuario (placeholder)");

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login endpoint via Gateway");
        response.put("status", "PLACEHOLDER");
        response.put("note", "Implementar proxy HTTP a auth-service");

        return ResponseEntity.ok(response);
    }

    /**
     * Registro de usuario
     * POST /api/v1/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> registerRequest) {
        logger.info("Gateway REST: Registro de usuario (placeholder)");

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Register endpoint via Gateway");
        response.put("status", "PLACEHOLDER");
        response.put("note", "Implementar proxy HTTP a auth-service");

        return ResponseEntity.ok(response);
    }

    /**
     * Validar token
     * POST /api/v1/auth/validate
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody Map<String, String> tokenRequest) {
        logger.info("Gateway REST: Validación de token (placeholder)");

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Validate token endpoint via Gateway");
        response.put("status", "PLACEHOLDER");
        response.put("note", "Implementar proxy HTTP a auth-service");

        return ResponseEntity.ok(response);
    }
}
