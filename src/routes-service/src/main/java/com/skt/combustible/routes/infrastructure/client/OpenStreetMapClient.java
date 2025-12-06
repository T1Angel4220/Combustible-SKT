package com.skt.combustible.routes.infrastructure.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cliente para interactuar con OpenStreetMap (Nominatim) y OpenRouteService
 * Alternativa gratuita a Google Maps API
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@Component
public class OpenStreetMapClient {

    private static final Logger logger = LoggerFactory.getLogger(OpenStreetMapClient.class);

    @Value("${openrouteservice.api.key:}")
    private String openRouteServiceKey;

    @Value("${openstreetmap.enabled:true}")
    private boolean enabled;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public OpenStreetMapClient() {
        this.objectMapper = new ObjectMapper();
        this.restTemplate = new RestTemplate();
        
        // Agregar User-Agent requerido por Nominatim
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new ClientHttpRequestInterceptor() {
            @Override
            public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
                request.getHeaders().add("User-Agent", "SKT-Fuel-System/1.0");
                return execution.execute(request, body);
            }
        });
        this.restTemplate.setInterceptors(interceptors);
    }

    /**
     * Calcula la distancia y duración entre dos ubicaciones usando OpenRouteService
     * 
     * @param origen Dirección del origen
     * @param destino Dirección del destino
     * @return Map con "distanciaKm" (Double) y "duracionHoras" (Double), o null si hay error
     */
    public Map<String, Double> calcularDistancia(String origen, String destino) {
        if (!enabled) {
            logger.warn("OpenStreetMap no está habilitado");
            return null;
        }

        try {
            // Primero geocodificar ambas direcciones para obtener coordenadas
            Map<String, Double> coordsOrigen = geocodificar(origen);
            Map<String, Double> coordsDestino = geocodificar(destino);

            if (coordsOrigen == null || coordsDestino == null) {
                logger.error("No se pudieron obtener coordenadas para origen o destino");
                return null;
            }

            // Si hay API key de OpenRouteService, usarla (más preciso)
            if (openRouteServiceKey != null && !openRouteServiceKey.isEmpty()) {
                return calcularDistanciaConOpenRouteService(coordsOrigen, coordsDestino);
            } else {
                // Usar fórmula de Haversine (distancia en línea recta)
                return calcularDistanciaHaversine(coordsOrigen, coordsDestino);
            }

        } catch (Exception e) {
            logger.error("Error calculando distancia: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Calcula la distancia directamente usando coordenadas (sin geocodificar)
     * 
     * @param origenCoords Map con "lat" y "lng" del origen
     * @param destinoCoords Map con "lat" y "lng" del destino
     * @return Map con "distanciaKm" (Double) y "duracionHoras" (Double), o null si hay error
     */
    public Map<String, Double> calcularDistanciaConCoordenadas(
            Map<String, Double> origenCoords, Map<String, Double> destinoCoords) {
        if (!enabled) {
            logger.warn("OpenStreetMap no está habilitado");
            return null;
        }

        try {
            // Si hay API key de OpenRouteService, usarla (más preciso)
            if (openRouteServiceKey != null && !openRouteServiceKey.isEmpty()) {
                return calcularDistanciaConOpenRouteService(origenCoords, destinoCoords);
            } else {
                // Usar fórmula de Haversine (distancia en línea recta)
                logger.info("Usando fórmula de Haversine para calcular distancia con coordenadas");
                return calcularDistanciaHaversine(origenCoords, destinoCoords);
            }
        } catch (Exception e) {
            logger.error("Error calculando distancia con coordenadas: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Calcula distancia usando OpenRouteService (más preciso, requiere API key gratuita)
     */
    private Map<String, Double> calcularDistanciaConOpenRouteService(
            Map<String, Double> origen, Map<String, Double> destino) {
        try {
            // Usar POST con JSON según la documentación de OpenRouteService v2
            String url = "https://api.openrouteservice.org/v2/directions/driving-car";
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", openRouteServiceKey);
            headers.set("Content-Type", "application/json");
            headers.set("Accept", "application/json, application/geo+json, application/gpx+xml, application/x-gpx+xml");
            
            String requestBody = String.format(
                "{\"coordinates\":[[%f,%f],[%f,%f]]}",
                origen.get("lng"), origen.get("lat"),
                destino.get("lng"), destino.get("lat")
            );
            
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(requestBody, headers);
            org.springframework.http.ResponseEntity<String> response = restTemplate.exchange(
                url, org.springframework.http.HttpMethod.POST, entity, String.class);
            
            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            if (jsonNode.has("error")) {
                logger.error("Error en OpenRouteService API: {}", jsonNode.get("error"));
                // Fallback a Haversine
                return calcularDistanciaHaversine(origen, destino);
            }

            JsonNode features = jsonNode.get("features");
            if (features == null || !features.isArray() || features.size() == 0) {
                logger.warn("No se encontraron rutas en OpenRouteService, usando Haversine");
                return calcularDistanciaHaversine(origen, destino);
            }

            JsonNode properties = features.get(0).get("properties");
            if (properties == null) {
                return calcularDistanciaHaversine(origen, destino);
            }
            
            JsonNode summary = properties.get("summary");
            if (summary == null) {
                return calcularDistanciaHaversine(origen, destino);
            }

            double distanciaMetros = summary.get("distance").asDouble();
            double distanciaKm = distanciaMetros / 1000.0;
            double duracionSegundos = summary.get("duration").asDouble();
            double duracionHoras = duracionSegundos / 3600.0;

            Map<String, Double> resultado = new HashMap<>();
            resultado.put("distanciaKm", distanciaKm);
            resultado.put("duracionHoras", duracionHoras);

            logger.info("Distancia calculada con OpenRouteService: {} km, Duración: {} horas", distanciaKm, duracionHoras);
            return resultado;

        } catch (Exception e) {
            logger.warn("Error con OpenRouteService, usando Haversine: {}", e.getMessage());
            return calcularDistanciaHaversine(origen, destino);
        }
    }

    /**
     * Calcula distancia usando fórmula de Haversine (distancia en línea recta)
     * No requiere API key, pero es menos preciso que usar rutas reales
     */
    private Map<String, Double> calcularDistanciaHaversine(
            Map<String, Double> origen, Map<String, Double> destino) {
        double lat1 = Math.toRadians(origen.get("lat"));
        double lon1 = Math.toRadians(origen.get("lng"));
        double lat2 = Math.toRadians(destino.get("lat"));
        double lon2 = Math.toRadians(destino.get("lng"));

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(lat1) * Math.cos(lat2) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distanciaKm = 6371 * c; // Radio de la Tierra en km

        // Estimar duración basada en velocidad promedio en Ecuador (60 km/h en carretera)
        // Aplicar factor de 1.3 para considerar que la distancia real es mayor que la línea recta
        double distanciaRealEstimada = distanciaKm * 1.3;
        double duracionHoras = distanciaRealEstimada / 60.0;

        Map<String, Double> resultado = new HashMap<>();
        resultado.put("distanciaKm", Math.round(distanciaRealEstimada * 100.0) / 100.0);
        resultado.put("duracionHoras", Math.round(duracionHoras * 100.0) / 100.0);

        logger.info("Distancia calculada con Haversine: {} km, Duración estimada: {} horas", 
            resultado.get("distanciaKm"), resultado.get("duracionHoras"));
        return resultado;
    }

    /**
     * Obtiene coordenadas (lat, lng) de una dirección usando Nominatim (OpenStreetMap)
     * 
     * @param direccion Dirección a geocodificar
     * @return Map con "lat" (Double) y "lng" (Double), o null si hay error
     */
    public Map<String, Double> geocodificar(String direccion) {
        if (!enabled) {
            logger.warn("OpenStreetMap no está habilitado");
            return null;
        }

        try {
            // Agregar "Ecuador" si no está presente para mejorar resultados
            String direccionCompleta = direccion;
            if (!direccion.toLowerCase().contains("ecuador")) {
                direccionCompleta = direccion + ", Ecuador";
            }

            String url = String.format(
                "https://nominatim.openstreetmap.org/search?q=%s&format=json&limit=1&countrycodes=ec",
                java.net.URLEncoder.encode(direccionCompleta, "UTF-8")
            );

            logger.debug("Consultando Nominatim API: direccion={}", direccion);

            // Esperar un poco para respetar rate limit de Nominatim (1 req/segundo)
            Thread.sleep(1100);

            String response = restTemplate.getForObject(url, String.class);
            JsonNode jsonNode = objectMapper.readTree(response);

            if (jsonNode == null || jsonNode.size() == 0) {
                logger.warn("No se encontraron resultados en Nominatim para: {}", direccion);
                return null;
            }

            JsonNode result = jsonNode.get(0);
            double lat = result.get("lat").asDouble();
            double lng = result.get("lon").asDouble();

            Map<String, Double> coordenadas = new HashMap<>();
            coordenadas.put("lat", lat);
            coordenadas.put("lng", lng);

            logger.debug("Coordenadas obtenidas: lat={}, lng={}", lat, lng);
            return coordenadas;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupción al geocodificar: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Error geocodificando dirección con Nominatim: {}", e.getMessage(), e);
            return null;
        }
    }
}

