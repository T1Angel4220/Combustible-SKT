package com.skt.combustible.vehicles.persistence.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Configuración de base de datos MongoDB para el servicio de vehículos
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Configuration
@EnableMongoRepositories(basePackages = "com.skt.combustible.vehicles.domain.repository")
public class DatabaseConfig extends AbstractMongoClientConfiguration {
    
    @Override
    protected String getDatabaseName() {
        return "vehicles_db";
    }
}
