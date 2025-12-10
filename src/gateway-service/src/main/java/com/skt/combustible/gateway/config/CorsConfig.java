package com.skt.combustible.gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración CORS global usando Spring MVC
 * Nota: Esta configuración actúa como respaldo. El CorsFilter tiene prioridad
 * y maneja los headers CORS a nivel de servlet antes de que llegue a Spring MVC.
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")  // Permitir todos los orígenes
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD")
                .allowedHeaders("*")
                .exposedHeaders("*")
                .allowCredentials(true)  // Permitir credentials para compatibilidad con el filtro
                .maxAge(3600);
    }
}
