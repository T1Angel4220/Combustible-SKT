package com.skt.combustible.vehicles.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * Configuración de perfiles para el servicio de vehículos
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Configuration
public class ProfileConfig {
    
    /**
     * Configuración para perfil de desarrollo
     */
    @Bean
    @Profile("dev")
    @Primary
    public String devProfile() {
        return "Development profile activated";
    }
    
    /**
     * Configuración para perfil de producción
     */
    @Bean
    @Profile("prod")
    @Primary
    public String prodProfile() {
        return "Production profile activated";
    }
    
    /**
     * Configuración para perfil de prueba
     */
    @Bean
    @Profile("test")
    @Primary
    public String testProfile() {
        return "Test profile activated";
    }
}
