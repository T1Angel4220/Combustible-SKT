package com.skt.combustible.vehicles.persistence.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuración de JPA para el servicio de vehículos
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.skt.combustible.vehicles.domain.repository")
@EnableTransactionManagement
public class JpaConfig {
    // Configuración adicional de JPA si es necesaria
}
