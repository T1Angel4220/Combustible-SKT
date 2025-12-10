package com.skt.combustible.gateway.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Controlador para servir archivos estáticos (HTML, CSS, JS) desde los servicios backend
 * Hace proxy de los archivos estáticos al servicio correspondiente
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@RestController
public class StaticResourceController {

    private static final Logger logger = LoggerFactory.getLogger(StaticResourceController.class);

    @Value("${service.urls.auth-service}")
    private String authServiceUrl;

    @Value("${service.urls.drivers-service}")
    private String driversServiceUrl;

    @Value("${service.urls.vehicles-service}")
    private String vehiclesServiceUrl;

    @Value("${service.urls.routes-service}")
    private String routesServiceUrl;

    @Value("${service.urls.fuel-service}")
    private String fuelServiceUrl;

    private final RestTemplate restTemplate;

    public StaticResourceController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Maneja index.html - viene del auth-service
     */
    @GetMapping({"/", "/index.html"})
    public ResponseEntity<String> getIndex(HttpServletRequest request) {
        return proxyStaticFile(authServiceUrl, "/index.html", request);
    }

    /**
     * Maneja dashboard.html - viene del auth-service
     */
    @GetMapping("/dashboard.html")
    public ResponseEntity<String> getDashboard(HttpServletRequest request) {
        return proxyStaticFile(authServiceUrl, "/dashboard.html", request);
    }

    /**
     * Maneja drivers.html - viene del drivers-service
     */
    @GetMapping("/drivers.html")
    public ResponseEntity<String> getDrivers(HttpServletRequest request) {
        return proxyStaticFile(driversServiceUrl, "/drivers.html", request);
    }

    /**
     * Maneja vehicles.html - viene del vehicles-service
     */
    @GetMapping("/vehicles.html")
    public ResponseEntity<String> getVehicles(HttpServletRequest request) {
        return proxyStaticFile(vehiclesServiceUrl, "/vehicles.html", request);
    }

    /**
     * Maneja routes.html - viene del routes-service
     */
    @GetMapping("/routes.html")
    public ResponseEntity<String> getRoutes(HttpServletRequest request) {
        return proxyStaticFile(routesServiceUrl, "/routes.html", request);
    }

    /**
     * Maneja fuel.html - viene del fuel-service
     */
    @GetMapping("/fuel.html")
    public ResponseEntity<String> getFuel(HttpServletRequest request) {
        return proxyStaticFile(fuelServiceUrl, "/fuel.html", request);
    }

    /**
     * Maneja archivos CSS, JS y otros recursos estáticos
     * Detecta automáticamente de qué servicio viene según la extensión y contexto
     * Usa múltiples endpoints específicos en lugar de regex con grupos de captura
     */
    @GetMapping(value = {
        "/{filename:.+\\.html}",
        "/{filename:.+\\.css}",
        "/{filename:.+\\.js}",
        "/{filename:.+\\.json}",
        "/{filename:.+\\.png}",
        "/{filename:.+\\.jpg}",
        "/{filename:.+\\.jpeg}",
        "/{filename:.+\\.gif}",
        "/{filename:.+\\.svg}",
        "/{filename:.+\\.ico}",
        "/{filename:.+\\.woff}",
        "/{filename:.+\\.woff2}",
        "/{filename:.+\\.ttf}",
        "/{filename:.+\\.eot}"
    })
    public ResponseEntity<Object> getStaticResource(
            @PathVariable String filename,
            HttpServletRequest request) {
        
        logger.debug("Proxy de archivo estático: {}", filename);
        
        // Determinar de qué servicio viene el archivo según el nombre
        String serviceUrl = determineServiceUrl(filename);
        
        if (serviceUrl == null) {
            // Por defecto, intentar desde auth-service
            serviceUrl = authServiceUrl;
        }
        
        // Archivos binarios (imágenes, fuentes)
        if (filename.endsWith(".png") || filename.endsWith(".jpg") || filename.endsWith(".jpeg") || 
            filename.endsWith(".gif") || filename.endsWith(".svg") || filename.endsWith(".ico") ||
            filename.endsWith(".woff") || filename.endsWith(".woff2") || filename.endsWith(".ttf") || 
            filename.endsWith(".eot")) {
            return proxyStaticFileBinary(serviceUrl, "/" + filename, request);
        }
        
        // Archivos de texto (HTML, CSS, JS, JSON)
        ResponseEntity<String> textResponse = proxyStaticFile(serviceUrl, "/" + filename, request);
        return ResponseEntity.status(textResponse.getStatusCode())
                .headers(textResponse.getHeaders())
                .body((Object) textResponse.getBody());
    }

    /**
     * Determina la URL del servicio basándose en el nombre del archivo
     */
    private String determineServiceUrl(String filename) {
        // Archivos específicos de servicios
        if (filename.contains("drivers") || filename.contains("driver")) {
            return driversServiceUrl;
        } else if (filename.contains("vehicles") || filename.contains("vehicle")) {
            return vehiclesServiceUrl;
        } else if (filename.contains("routes") || filename.contains("route")) {
            return routesServiceUrl;
        } else if (filename.contains("fuel")) {
            return fuelServiceUrl;
        } else if (filename.contains("dashboard") || filename.contains("index") || 
                   (filename.contains("script.js") && !filename.contains("drivers") && 
                    !filename.contains("vehicles") && !filename.contains("routes") && 
                    !filename.contains("fuel"))) {
            return authServiceUrl;
        }
        return null; // Por defecto auth-service
    }

    /**
     * Hace proxy de un archivo estático desde el servicio backend
     */
    private ResponseEntity<String> proxyStaticFile(String serviceUrl, String filePath, HttpServletRequest request) {
        try {
            String url = serviceUrl + filePath;
            logger.debug("Haciendo proxy de archivo estático: {} -> {}", filePath, url);
            
            HttpHeaders headers = new HttpHeaders();
            // Copiar headers relevantes del request original
            String acceptHeader = request.getHeader("Accept");
            if (acceptHeader != null) {
                headers.set("Accept", acceptHeader);
            }
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
            );
            
            // Determinar Content-Type según la extensión del archivo
            HttpHeaders responseHeaders = new HttpHeaders();
            String contentType = determineContentType(filePath);
            responseHeaders.setContentType(MediaType.parseMediaType(contentType));
            
            return new ResponseEntity<>(response.getBody(), responseHeaders, response.getStatusCode());
            
        } catch (Exception e) {
            logger.error("Error haciendo proxy de archivo estático {}: {}", filePath, e.getMessage());
            
            // Si es un HTML, devolver un error HTML básico
            if (filePath.endsWith(".html")) {
                String errorHtml = "<!DOCTYPE html><html><head><title>Error</title></head>" +
                    "<body><h1>Error 404</h1><p>Archivo no encontrado: " + filePath + "</p></body></html>";
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.TEXT_HTML)
                    .body(errorHtml);
            }
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Hace proxy de un archivo binario estático desde el servicio backend
     */
    private ResponseEntity<Object> proxyStaticFileBinary(String serviceUrl, String filePath, HttpServletRequest request) {
        try {
            String url = serviceUrl + filePath;
            logger.debug("Haciendo proxy de archivo binario: {} -> {}", filePath, url);
            
            HttpHeaders headers = new HttpHeaders();
            String acceptHeader = request.getHeader("Accept");
            if (acceptHeader != null) {
                headers.set("Accept", acceptHeader);
            }
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<byte[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                byte[].class
            );
            
            HttpHeaders responseHeaders = new HttpHeaders();
            String contentType = determineContentType(filePath);
            responseHeaders.setContentType(MediaType.parseMediaType(contentType));
            
            return new ResponseEntity<>(response.getBody(), responseHeaders, response.getStatusCode());
            
        } catch (Exception e) {
            logger.error("Error haciendo proxy de archivo binario {}: {}", filePath, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Determina el Content-Type según la extensión del archivo
     */
    private String determineContentType(String filePath) {
        if (filePath.endsWith(".html")) {
            return "text/html;charset=UTF-8";
        } else if (filePath.endsWith(".css")) {
            return "text/css;charset=UTF-8";
        } else if (filePath.endsWith(".js")) {
            return "application/javascript;charset=UTF-8";
        } else if (filePath.endsWith(".json")) {
            return "application/json;charset=UTF-8";
        } else if (filePath.endsWith(".png")) {
            return "image/png";
        } else if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (filePath.endsWith(".gif")) {
            return "image/gif";
        } else if (filePath.endsWith(".svg")) {
            return "image/svg+xml";
        } else if (filePath.endsWith(".ico")) {
            return "image/x-icon";
        } else if (filePath.endsWith(".woff") || filePath.endsWith(".woff2")) {
            return "font/woff2";
        } else if (filePath.endsWith(".ttf")) {
            return "font/ttf";
        }
        
        return "text/plain;charset=UTF-8";
    }
}

