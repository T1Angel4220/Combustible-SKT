package com.skt.combustible.gateway.service;

import io.grpc.ManagedChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Servicio para manejar clientes gRPC en el Gateway
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Service
public class GrpcClientService {

    @Autowired
    @Qualifier("driversGrpcChannel")
    private ManagedChannel driversChannel;

    @Autowired
    @Qualifier("vehiclesGrpcChannel")
    private ManagedChannel vehiclesChannel;

    @Autowired
    @Qualifier("routesGrpcChannel")
    private ManagedChannel routesChannel;

    @Autowired
    @Qualifier("fuelGrpcChannel")
    private ManagedChannel fuelChannel;

    @Autowired
    @Qualifier("authGrpcChannel")
    private ManagedChannel authChannel;

    /**
     * Obtiene el canal gRPC para Drivers Service
     */
    public ManagedChannel getDriversChannel() {
        return driversChannel;
    }

    /**
     * Obtiene el canal gRPC para Vehicles Service
     */
    public ManagedChannel getVehiclesChannel() {
        return vehiclesChannel;
    }

    /**
     * Obtiene el canal gRPC para Routes Service
     */
    public ManagedChannel getRoutesChannel() {
        return routesChannel;
    }

    /**
     * Obtiene el canal gRPC para Fuel Service
     */
    public ManagedChannel getFuelChannel() {
        return fuelChannel;
    }

    /**
     * Obtiene el canal gRPC para Auth Service
     */
    public ManagedChannel getAuthChannel() {
        return authChannel;
    }
}
