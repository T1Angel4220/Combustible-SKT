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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cliente REST para comunicarse con el Vehicles Service para obtener asignaciones
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@Component
public class AssignmentsRestClient {

    private static final Logger logger = LoggerFactory.getLogger(AssignmentsRestClient.class);

    @Value("${service.urls.vehicles-service:http://localhost:8082}")
    private String vehiclesServiceUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AssignmentsRestClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Obtiene las asignaciones activas de un conductor
     * 
     * @param choferId ID del conductor
     * @param token Token JWT para autenticación
     * @return Lista de asignaciones activas
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getActiveAssignmentsByDriver(String choferId, String token) {
        try {
            String url = vehiclesServiceUrl + "/api/v1/assignments/chofer/" + choferId;
            
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
                List<Map<String, Object>> asignaciones = new ArrayList<>();
                
                if (response.getBody() instanceof List) {
                    List<?> bodyList = (List<?>) response.getBody();
                    for (Object item : bodyList) {
                        if (item instanceof Map) {
                            asignaciones.add((Map<String, Object>) item);
                        } else {
                            Map<String, Object> map = objectMapper.convertValue(item, Map.class);
                            asignaciones.add(map);
                        }
                    }
                } else if (response.getBody() instanceof Map) {
                    asignaciones.add((Map<String, Object>) response.getBody());
                } else {
                    Map<String, Object> map = objectMapper.convertValue(response.getBody(), Map.class);
                    asignaciones.add(map);
                }
                
                // Filtrar solo asignaciones activas
                asignaciones.removeIf(a -> {
                    Object estado = a.get("estado");
                    return estado == null || !"ACTIVA".equals(estado.toString());
                });
                
                logger.debug("Asignaciones activas encontradas para chofer {}: {}", choferId, asignaciones.size());
                return asignaciones;
            }
            
            return new ArrayList<>();
        } catch (Exception e) {
            logger.error("Error obteniendo asignaciones activas para chofer {}: {}", choferId, e.getMessage());
            return new ArrayList<>();
        }
    }
}

