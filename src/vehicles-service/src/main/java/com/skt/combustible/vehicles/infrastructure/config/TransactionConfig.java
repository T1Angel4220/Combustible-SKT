package com.skt.combustible.vehicles.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Configuración de MongoDB
 * Spring Boot configurará automáticamente MongoDB usando la URI del application.yml
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Configuration
@EnableMongoRepositories(basePackages = "com.skt.combustible.vehicles.domain.repository")
public class TransactionConfig {
    // Spring Boot configurará MongoDB automáticamente desde application.yml
    // No necesitamos extender AbstractMongoClientConfiguration ya que interfiere
    // con la configuración automática de Spring Boot
}
