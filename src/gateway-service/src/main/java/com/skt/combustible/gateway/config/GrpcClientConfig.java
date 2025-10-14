package com.skt.combustible.gateway.config;

import com.skt.combustible.gateway.infrastructure.client.DriversGrpcClient;
import com.skt.combustible.gateway.infrastructure.mapper.DriverMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de clientes gRPC para el Gateway Service
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@Configuration
public class GrpcClientConfig {

    /**
     * Bean para el cliente gRPC de Drivers
     */
    @Bean
    public DriversGrpcClient driversGrpcClient() {
        return new DriversGrpcClient();
    }

    /**
     * Bean para el mapper de Drivers
     */
    @Bean
    public DriverMapper driverMapper() {
        return new DriverMapper();
    }
}
