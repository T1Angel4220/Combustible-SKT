package com.skt.combustible.vehicles.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import com.skt.combustible.vehicles.domain.repository.VehicleRepository;

/**
 * Configuración de monitoreo para el servicio de vehículos
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Configuration
public class MonitoringConfig {
    
    @Autowired
    private VehicleRepository vehicleRepository;
    
    /**
     * Health indicator personalizado para el servicio de vehículos
     */
    @Bean
    @Primary
    public HealthIndicator vehicleServiceHealthIndicator() {
        return () -> {
            try {
                long totalVehicles = vehicleRepository.count();
                return Health.up()
                    .withDetail("service", "vehicles-service")
                    .withDetail("totalVehicles", totalVehicles)
                    .withDetail("status", "UP")
                    .build();
            } catch (Exception e) {
                return Health.down()
                    .withDetail("service", "vehicles-service")
                    .withDetail("error", e.getMessage())
                    .withDetail("status", "DOWN")
                    .build();
            }
        };
    }
}
