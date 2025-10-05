package com.skt.combustible.vehicles.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import io.grpc.ServerBuilder;
import net.devh.boot.grpc.server.config.GrpcServerProperties;

/**
 * Configuración de gRPC para el servicio de vehículos
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Configuration
public class GrpcConfig {
    
    /**
     * Configuración del servidor gRPC
     */
    @Bean
    @Primary
    public ServerBuilder<?> grpcServerBuilder(GrpcServerProperties properties) {
        return ServerBuilder.forPort(properties.getPort());
    }
}
