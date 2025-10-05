package com.skt.combustible.vehicles.persistence.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuración de base de datos para el servicio de vehículos
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Configuration
@EntityScan(basePackages = "com.skt.combustible.vehicles.domain.entity")
@EnableJpaRepositories(basePackages = "com.skt.combustible.vehicles.domain.repository")
@EnableTransactionManagement
public class DatabaseConfig {
    // Configuración adicional de base de datos si es necesaria
}
