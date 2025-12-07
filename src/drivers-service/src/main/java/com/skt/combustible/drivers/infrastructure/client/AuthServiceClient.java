package com.skt.combustible.drivers.infrastructure.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skt.combustible.shared.domain.enums.RolUsuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Cliente REST para comunicarse con el Auth Service
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@Component
public class AuthServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceClient.class);

    @Value("${service.urls.auth-service:http://localhost:8085}")
    private String authServiceUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Crea un nuevo usuario en el auth-service
     * 
     * @param username  username del usuario
     * @param email     email del usuario
     * @param password  contraseña del usuario
     * @param nombre    nombre del usuario
     * @param apellido  apellido del usuario
     * @param rol       rol del usuario
     * @param token     token JWT del admin que está creando el usuario
     * @return ID del usuario creado
     * @throws RuntimeException si hay error creando el usuario
     */
    public String createUser(String username, String email, String password, String nombre, 
                            String apellido, RolUsuario rol, String token) {
        try {
            logger.info("Creando usuario en auth-service: {}", username);

            String url = authServiceUrl + "/api/users";

            // Preparar el request
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("username", username);
            requestBody.put("email", email);
            requestBody.put("password", password);
            requestBody.put("nombre", nombre);
            requestBody.put("apellido", apellido);
            requestBody.put("rol", rol.name());

            // Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + token);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // Llamar al servicio
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody.containsKey("user")) {
                    Map<String, Object> user = (Map<String, Object>) responseBody.get("user");
                    String userId = (String) user.get("id");
                    logger.info("Usuario creado exitosamente en auth-service con ID: {}", userId);
                    return userId;
                }
            }

            logger.warn("Respuesta inesperada del auth-service al crear usuario");
            throw new RuntimeException("Error creando usuario: respuesta inesperada del auth-service");

        } catch (Exception e) {
            logger.error("Error creando usuario en auth-service: {}", e.getMessage(), e);
            throw new RuntimeException("Error creando usuario en auth-service: " + e.getMessage(), e);
        }
    }
}

