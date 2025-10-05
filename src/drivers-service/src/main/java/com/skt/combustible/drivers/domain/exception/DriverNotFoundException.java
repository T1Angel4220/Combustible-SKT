package com.skt.combustible.drivers.domain.exception;

/**
 * Excepción lanzada cuando un chofer no es encontrado
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
public class DriverNotFoundException extends DriverServiceException {
    
    public DriverNotFoundException(String message) {
        super(message);
    }
    
    public DriverNotFoundException(Long id) {
        super("Chofer con ID " + id + " no encontrado");
    }
    
    public DriverNotFoundException(String field, String value) {
        super("Chofer con " + field + " '" + value + "' no encontrado");
    }
    
    // Constructor específico para ID como String (MongoDB)
    public static DriverNotFoundException withId(String id) {
        return new DriverNotFoundException("Chofer con ID " + id + " no encontrado");
    }
}
