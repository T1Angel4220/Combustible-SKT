package com.skt.combustible.gateway.config;

import org.springframework.context.annotation.Configuration;

/**
 * Configuración CORS deshabilitada - El filtro CorsFilter maneja todo
 * Esto evita conflictos entre múltiples configuraciones de CORS
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@Configuration
public class CorsConfig {
    // Toda la configuración CORS se maneja en CorsFilter
    // No necesitamos configuraciones adicionales que puedan causar conflictos
}
