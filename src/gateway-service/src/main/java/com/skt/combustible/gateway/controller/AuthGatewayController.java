package com.skt.combustible.gateway.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
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
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.PATCH})
public class AuthGatewayController {

    private static final Logger logger = LoggerFactory.getLogger(AuthGatewayController.class);

    @Value("${service.urls.auth-service}")
    private String authServiceUrl;

    private final RestTemplate restTemplate;

    public AuthGatewayController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private HttpHeaders getHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            headers.set("Authorization", authorizationHeader);
        }
        return headers;
    }

    /**
     * Maneja preflight OPTIONS requests para CORS
     */
    @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> handleOptions() {
        return ResponseEntity.ok().build();
    }

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
    public ResponseEntity<Object> login(@RequestBody Map<String, String> loginRequest, HttpServletRequest request) {
        logger.info("Gateway REST: Login de usuario via proxy a auth-service");
        logger.info("Gateway REST: authServiceUrl configurado: {}", authServiceUrl);

        try {
            String url = authServiceUrl + "/api/auth/login";
            logger.info("Gateway REST: Intentando conectar a: {}", url);
            
            HttpHeaders headers = getHeaders(request);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(loginRequest, headers);

            ResponseEntity<Object> response = restTemplate.postForEntity(url, entity, Object.class);
            logger.info("Gateway REST: Login exitoso via auth-service, status: {}", response.getStatusCode());
            
            return response;

        } catch (org.springframework.web.client.ResourceAccessException e) {
            logger.error("Gateway REST: Error de conexión con auth-service (no se pudo conectar): {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error de conexión con auth-service. URL: " + authServiceUrl);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("type", "CONNECTION_ERROR");
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorResponse);
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            logger.error("Gateway REST: Error HTTP del cliente en auth-service: {}", e.getMessage());
            // Retornar el error del servicio directamente
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAs(Map.class));
        } catch (Exception e) {
            logger.error("Gateway REST: Error en login via auth-service: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error de conexión con auth-service");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("type", "UNKNOWN_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Registro de usuario
     * POST /api/v1/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody Map<String, String> registerRequest,
            HttpServletRequest request) {
        logger.info("Gateway REST: Registro de usuario via proxy a auth-service");

        try {
            String url = authServiceUrl + "/api/auth/register";
            HttpHeaders headers = getHeaders(request);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(registerRequest, headers);

            ResponseEntity<Object> response = restTemplate.postForEntity(url, entity, Object.class);
            logger.info("Gateway REST: Registro exitoso via auth-service");
            return response;

        } catch (Exception e) {
            logger.error("Gateway REST: Error en registro via auth-service: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error de conexión con auth-service");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
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
