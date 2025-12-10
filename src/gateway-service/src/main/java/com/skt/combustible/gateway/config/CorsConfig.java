package com.skt.combustible.gateway.config;

// import org.springframework.context.annotation.Configuration;  // No usado - clase deshabilitada
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración CORS global usando Spring MVC
 * NOTA: Esta configuración está DESHABILITADA porque el CorsFilter maneja todo.
 * Si se habilita, usar allowedOriginPatterns con allowCredentials(true) causa conflicto.
 * El CorsFilter tiene prioridad y maneja los headers CORS correctamente a nivel de servlet.
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
// @Configuration  // Deshabilitado - el CorsFilter maneja todo
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Deshabilitado - usar CorsFilter en su lugar
        // Si se necesita habilitar, NO usar allowCredentials(true) con allowedOriginPatterns("*")
        // registry.addMapping("/**")
        //         .allowedOriginPatterns("*")
        //         .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD")
        //         .allowedHeaders("*")
        //         .exposedHeaders("*")
        //         .allowCredentials(false)  // false si usas "*"
        //         .maxAge(3600);
    }
}
