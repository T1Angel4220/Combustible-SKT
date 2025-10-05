package com.skt.combustible.vehicles.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Configuración de propiedades para el servicio de vehículos
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Configuration
@EnableConfigurationProperties
public class PropertiesConfig {
    
    /**
     * Propiedades del servicio de vehículos
     */
    @Bean
    @Primary
    @ConfigurationProperties(prefix = "vehicles.service")
    public VehicleServiceProperties vehicleServiceProperties() {
        return new VehicleServiceProperties();
    }
    
    /**
     * Clase de propiedades del servicio de vehículos
     */
    public static class VehicleServiceProperties {
        private String name = "vehicles-service";
        private String version = "1.0.0";
        private String description = "Servicio de gestión de vehículos";
        
        // Getters y Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
