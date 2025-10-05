package com.skt.combustible.vehicles.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuración de logging para el servicio de vehículos
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Configuration
public class LoggingConfig {
    
    /**
     * Logger principal del servicio de vehículos
     */
    @Bean
    @Primary
    public Logger vehicleServiceLogger() {
        return LoggerFactory.getLogger("com.skt.combustible.vehicles");
    }
}
