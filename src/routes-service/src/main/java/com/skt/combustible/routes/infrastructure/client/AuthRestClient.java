package com.skt.combustible.routes.infrastructure.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Cliente REST para comunicarse con el Auth Service desde Routes Service
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@Component
public class AuthRestClient {

    private static final Logger logger = LoggerFactory.getLogger(AuthRestClient.class);

    @Value("${service.urls.auth-service:http://localhost:8085}")
    private String authServiceUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AuthRestClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Obtiene el userId del usuario desde el auth-service usando el token
     * 
     * @param token Token JWT para autenticación
     * @return ID del usuario o null si no se encuentra
     */
    public String getUserIdFromToken(String token) {
        try {
            String url = authServiceUrl + "/api/auth/me";
            
            HttpHeaders headers = new HttpHeaders();
            if (token != null && !token.isEmpty()) {
                headers.set("Authorization", "Bearer " + token);
            }
            headers.set("Content-Type", "application/json");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Object> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Object.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> userData;
                if (response.getBody() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) response.getBody();
                    userData = data;
                } else {
                    userData = objectMapper.convertValue(response.getBody(), Map.class);
                }
                
                // El userId puede estar en "id" o "userId"
                Object userId = userData.get("id");
                if (userId == null) {
                    userId = userData.get("userId");
                }
                
                if (userId != null) {
                    logger.debug("UserId obtenido desde auth-service: {}", userId);
                    return userId.toString();
                }
            }
            
            logger.warn("No se pudo obtener userId desde auth-service");
            return null;
        } catch (Exception e) {
            logger.error("Error obteniendo userId desde auth-service: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene el email del usuario desde el auth-service usando el token
     * 
     * @param token Token JWT para autenticación
     * @return Email del usuario o null si no se encuentra
     */
    public String getEmailFromToken(String token) {
        try {
            String url = authServiceUrl + "/api/auth/me";
            
            HttpHeaders headers = new HttpHeaders();
            if (token != null && !token.isEmpty()) {
                headers.set("Authorization", "Bearer " + token);
            }
            headers.set("Content-Type", "application/json");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Object> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Object.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> userData;
                if (response.getBody() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) response.getBody();
                    userData = data;
                } else {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = objectMapper.convertValue(response.getBody(), Map.class);
                    userData = data;
                }
                
                Object email = userData.get("email");
                if (email != null) {
                    logger.debug("Email obtenido desde auth-service: {}", email);
                    return email.toString();
                }
            }
            
            logger.warn("No se pudo obtener email desde auth-service");
            return null;
        } catch (Exception e) {
            logger.error("Error obteniendo email desde auth-service: {}", e.getMessage());
            return null;
        }
    }
}

