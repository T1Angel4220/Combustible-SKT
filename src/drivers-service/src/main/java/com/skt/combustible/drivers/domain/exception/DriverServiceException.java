package com.skt.combustible.drivers.domain.exception;

/**
 * Excepción base para el servicio de choferes
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
public class DriverServiceException extends RuntimeException {
    
    public DriverServiceException(String message) {
        super(message);
    }
    
    public DriverServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
