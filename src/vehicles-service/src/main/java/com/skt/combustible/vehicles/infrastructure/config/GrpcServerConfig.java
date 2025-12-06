package com.skt.combustible.vehicles.infrastructure.config;

import net.devh.boot.grpc.server.config.GrpcServerProperties;
import net.devh.boot.grpc.server.service.GrpcServiceDefinition;
import net.devh.boot.grpc.server.service.GrpcServiceDiscoverer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import net.devh.boot.grpc.server.service.AnnotationGrpcServiceDiscoverer;

import jakarta.annotation.PostConstruct;
import java.util.Collection;

/**
 * Configuración específica para el servidor gRPC
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@Configuration
@EnableConfigurationProperties(GrpcServerProperties.class)
public class GrpcServerConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(GrpcServerConfig.class);
    
    @PostConstruct
    public void logGrpcConfiguration() {
        logger.info("=== CONFIGURACIÓN gRPC INICIALIZADA ===");
        logger.info("Forzando el inicio del servidor gRPC en puerto 9092");
    }
    
    /**
     * Bean personalizado para descubrir servicios gRPC
     */
    @Bean
    public GrpcServiceDiscoverer grpcServiceDiscoverer() {
        return new AnnotationGrpcServiceDiscoverer() {
            @Override
            public Collection<GrpcServiceDefinition> findGrpcServices() {
                Collection<GrpcServiceDefinition> services = super.findGrpcServices();
                logger.info("=== SERVICIOS gRPC ENCONTRADOS: {} ===", services.size());
                for (GrpcServiceDefinition service : services) {
                    logger.info("Servicio gRPC detectado: {}", service.getClass().getSimpleName());
                }
                if (services.isEmpty()) {
                    logger.error("NO SE ENCONTRARON SERVICIOS gRPC - Verificar que los controladores tengan @GrpcService");
                }
                return services;
            }
        };
    }
}

