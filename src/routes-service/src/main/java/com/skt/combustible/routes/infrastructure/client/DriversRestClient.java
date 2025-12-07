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

import java.util.HashMap;
import java.util.Map;

/**
 * Cliente REST para comunicarse con el Drivers Service desde Routes Service
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@Component
public class DriversRestClient {

    private static final Logger logger = LoggerFactory.getLogger(DriversRestClient.class);

    @Value("${service.urls.drivers-service:http://localhost:8081}")
    private String driversServiceUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public DriversRestClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Obtiene el conductor asociado a un usuario por usuarioId
     * 
     * @param usuarioId ID del usuario en auth-service
     * @param token Token JWT para autenticación
     * @return Map con los datos del conductor o null si no se encuentra
     */
    public Map<String, Object> getDriverByUsuarioId(String usuarioId, String token) {
        try {
            String url = driversServiceUrl + "/api/v1/drivers/by-usuario/" + usuarioId;
            
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
                if (response.getBody() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> driver = (Map<String, Object>) response.getBody();
                    logger.debug("Conductor encontrado para usuarioId {}: {}", usuarioId, driver.get("id"));
                    return driver;
                } else {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> driver = objectMapper.convertValue(response.getBody(), Map.class);
                    logger.debug("Conductor encontrado para usuarioId {}: {}", usuarioId, driver.get("id"));
                    return driver;
                }
            }
            
            logger.warn("No se encontró conductor para usuarioId: {}", usuarioId);
            return null;
        } catch (Exception e) {
            logger.error("Error obteniendo conductor por usuarioId {}: {}", usuarioId, e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene el conductor asociado a un usuario por email (fallback)
     * 
     * @param email Email del usuario
     * @param token Token JWT para autenticación
     * @return Map con los datos del conductor o null si no se encuentra
     */
    public Map<String, Object> getDriverByEmail(String email, String token) {
        try {
            String url = driversServiceUrl + "/api/v1/drivers/by-email/" + java.net.URLEncoder.encode(email, "UTF-8");
            
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
                if (response.getBody() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> driver = (Map<String, Object>) response.getBody();
                    logger.debug("Conductor encontrado para email {}: {}", email, driver.get("id"));
                    return driver;
                } else {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> driver = objectMapper.convertValue(response.getBody(), Map.class);
                    logger.debug("Conductor encontrado para email {}: {}", email, driver.get("id"));
                    return driver;
                }
            }
            
            logger.warn("No se encontró conductor para email: {}", email);
            return null;
        } catch (Exception e) {
            logger.error("Error obteniendo conductor por email {}: {}", email, e.getMessage());
            return null;
        }
    }
}

