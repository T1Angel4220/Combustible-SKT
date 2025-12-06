package com.skt.combustible.routes.domain.exception;

/**
 * Excepción lanzada cuando no se encuentra una ruta
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
public class RouteNotFoundException extends RuntimeException {
    
    public RouteNotFoundException(String message) {
        super(message);
    }
    
    public static RouteNotFoundException withId(String id) {
        return new RouteNotFoundException("Ruta no encontrada con ID: " + id);
    }
    
    public static RouteNotFoundException withCodigo(String codigo) {
        return new RouteNotFoundException("Ruta no encontrada con código: " + codigo);
    }
}

