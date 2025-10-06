package com.skt.combustible.drivers.domain.exception;

/**
 * Excepción lanzada cuando se intenta crear un chofer con datos duplicados
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
public class DriverDuplicateException extends DriverServiceException {
    
    public DriverDuplicateException(String message) {
        super(message);
    }
    
    public DriverDuplicateException(String field, String value) {
        super("Ya existe un chofer con " + field + " '" + value + "'");
    }
}
