package com.skt.combustible.vehicles.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

/**
 * Configuración de transacciones para el servicio de vehículos
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Configuration
@EnableTransactionManagement
public class TransactionConfig {
    
    /**
     * Template de transacciones para operaciones programáticas
     */
    @Bean
    public TransactionTemplate transactionTemplate() {
        return new TransactionTemplate();
    }
}
