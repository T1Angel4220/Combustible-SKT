package com.skt.combustible.gateway.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración personalizada de gRPC para el Gateway
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Configuration
public class GrpcConfig {

    /**
     * Canal gRPC para Drivers Service
     */
    @Bean("driversGrpcChannel")
    public ManagedChannel driversGrpcChannel() {
        return ManagedChannelBuilder.forAddress("localhost", 9091)
                .usePlaintext()
                .build();
    }

    /**
     * Canal gRPC para Vehicles Service
     */
    @Bean("vehiclesGrpcChannel")
    public ManagedChannel vehiclesGrpcChannel() {
        return ManagedChannelBuilder.forAddress("localhost", 9092)
                .usePlaintext()
                .build();
    }

    /**
     * Canal gRPC para Routes Service
     */
    @Bean("routesGrpcChannel")
    public ManagedChannel routesGrpcChannel() {
        return ManagedChannelBuilder.forAddress("localhost", 9093)
                .usePlaintext()
                .build();
    }

    /**
     * Canal gRPC para Fuel Service
     */
    @Bean("fuelGrpcChannel")
    public ManagedChannel fuelGrpcChannel() {
        return ManagedChannelBuilder.forAddress("localhost", 9094)
                .usePlaintext()
                .build();
    }

    /**
     * Canal gRPC para Auth Service
     */
    @Bean("authGrpcChannel")
    public ManagedChannel authGrpcChannel() {
        return ManagedChannelBuilder.forAddress("localhost", 9095)
                .usePlaintext()
                .build();
    }
}
