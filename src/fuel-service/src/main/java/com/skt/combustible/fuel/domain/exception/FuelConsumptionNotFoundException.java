package com.skt.combustible.fuel.domain.exception;

/**
 * Excepción lanzada cuando no se encuentra un registro de consumo de combustible
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
public class FuelConsumptionNotFoundException extends RuntimeException {
    
    public FuelConsumptionNotFoundException(String message) {
        super(message);
    }
    
    public static FuelConsumptionNotFoundException withId(String id) {
        return new FuelConsumptionNotFoundException("Registro de combustible no encontrado con ID: " + id);
    }
}

