package com.skt.combustible.vehicles.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Configuración de eventos para el servicio de vehículos
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Configuration
public class EventConfig {
    
    /**
     * Publisher de eventos personalizado
     */
    @Bean
    @Primary
    public ApplicationEventPublisher customEventPublisher() {
        return new ApplicationEventPublisher() {
            @Override
            public void publishEvent(Object event) {
                // Implementar lógica de publicación de eventos si es necesario
            }
        };
    }
}
