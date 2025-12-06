package com.skt.combustible.routes.infrastructure.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Cliente para interactuar con Google Maps API
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@Component
public class GoogleMapsClient {

    private static final Logger logger = LoggerFactory.getLogger(GoogleMapsClient.class);

    @Value("${google.maps.api.key:}")
    private String apiKey;

    @Value("${google.maps.api.enabled:false}")
    private boolean enabled;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GoogleMapsClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Calcula la distancia y duración entre dos ubicaciones usando Google Maps Distance Matrix API
     * 
     * @param origen Dirección o coordenadas del origen (formato: "dirección" o "lat,lng")
     * @param destino Dirección o coordenadas del destino (formato: "dirección" o "lat,lng")
     * @return Map con "distanciaKm" (Double) y "duracionHoras" (Double), o null si hay error
     */
    public Map<String, Double> calcularDistancia(String origen, String destino) {
        if (!enabled || apiKey == null || apiKey.isEmpty()) {
            logger.warn("Google Maps API no está habilitada o no hay API key configurada");
            return null;
        }

        try {
            // Restringir búsquedas a Ecuador
            String origenRestricted = origen + ", Ecuador";
            String destinoRestricted = destino + ", Ecuador";

            String url = String.format(
                "https://maps.googleapis.com/maps/api/distancematrix/json?origins=%s&destinations=%s&key=%s&units=metric&language=es",
                java.net.URLEncoder.encode(origenRestricted, "UTF-8"),
                java.net.URLEncoder.encode(destinoRestricted, "UTF-8"),
                apiKey
            );

            logger.debug("Consultando Google Maps Distance Matrix API: origen={}, destino={}", origen, destino);

            String response = restTemplate.getForObject(url, String.class);
            JsonNode jsonNode = objectMapper.readTree(response);

            String status = jsonNode.get("status").asText();
            if (!"OK".equals(status)) {
                logger.error("Error en Google Maps API: {}", status);
                return null;
            }

            JsonNode rows = jsonNode.get("rows");
            if (rows == null || rows.size() == 0) {
                logger.error("No se encontraron resultados en Google Maps API");
                return null;
            }

            JsonNode elements = rows.get(0).get("elements");
            if (elements == null || elements.size() == 0) {
                logger.error("No se encontraron elementos en la respuesta de Google Maps API");
                return null;
            }

            JsonNode element = elements.get(0);
            String elementStatus = element.get("status").asText();
            if (!"OK".equals(elementStatus)) {
                logger.error("Error en elemento de Google Maps API: {}", elementStatus);
                return null;
            }

            // Extraer distancia (en metros)
            JsonNode distance = element.get("distance");
            double distanciaMetros = distance.get("value").asDouble();
            double distanciaKm = distanciaMetros / 1000.0;

            // Extraer duración (en segundos)
            JsonNode duration = element.get("duration");
            double duracionSegundos = duration.get("value").asDouble();
            double duracionHoras = duracionSegundos / 3600.0;

            Map<String, Double> resultado = new HashMap<>();
            resultado.put("distanciaKm", distanciaKm);
            resultado.put("duracionHoras", duracionHoras);

            logger.info("Distancia calculada: {} km, Duración: {} horas", distanciaKm, duracionHoras);

            return resultado;

        } catch (Exception e) {
            logger.error("Error calculando distancia con Google Maps API: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Obtiene coordenadas (lat, lng) de una dirección usando Google Maps Geocoding API
     * 
     * @param direccion Dirección a geocodificar
     * @return Map con "lat" (Double) y "lng" (Double), o null si hay error
     */
    public Map<String, Double> geocodificar(String direccion) {
        if (!enabled || apiKey == null || apiKey.isEmpty()) {
            logger.warn("Google Maps API no está habilitada o no hay API key configurada");
            return null;
        }

        try {
            // Restringir búsquedas a Ecuador
            String direccionRestricted = direccion + ", Ecuador";

            String url = String.format(
                "https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=%s&region=ec",
                java.net.URLEncoder.encode(direccionRestricted, "UTF-8"),
                apiKey
            );

            logger.debug("Consultando Google Maps Geocoding API: direccion={}", direccion);

            String response = restTemplate.getForObject(url, String.class);
            JsonNode jsonNode = objectMapper.readTree(response);

            String status = jsonNode.get("status").asText();
            if (!"OK".equals(status)) {
                logger.error("Error en Google Maps Geocoding API: {}", status);
                return null;
            }

            JsonNode results = jsonNode.get("results");
            if (results == null || results.size() == 0) {
                logger.error("No se encontraron resultados en Google Maps Geocoding API");
                return null;
            }

            JsonNode location = results.get(0).get("geometry").get("location");
            double lat = location.get("lat").asDouble();
            double lng = location.get("lng").asDouble();

            Map<String, Double> coordenadas = new HashMap<>();
            coordenadas.put("lat", lat);
            coordenadas.put("lng", lng);

            logger.debug("Coordenadas obtenidas: lat={}, lng={}", lat, lng);

            return coordenadas;

        } catch (Exception e) {
            logger.error("Error geocodificando dirección con Google Maps API: {}", e.getMessage(), e);
            return null;
        }
    }
}

