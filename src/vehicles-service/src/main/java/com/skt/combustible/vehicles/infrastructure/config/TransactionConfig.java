package com.skt.combustible.vehicles.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Configuración de MongoDB
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Configuration
@EnableMongoRepositories(basePackages = "com.skt.combustible.vehicles.domain.repository")
public class TransactionConfig extends AbstractMongoClientConfiguration {
    
    @Override
    protected String getDatabaseName() {
        return "vehicles_db";
    }
}
