package com.skt.combustible.routes.domain.exception;

/**
 * Excepción lanzada cuando se intenta crear una ruta con datos duplicados
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
public class RouteDuplicateException extends RuntimeException {
    
    public RouteDuplicateException(String message) {
        super(message);
    }
    
    public static RouteDuplicateException withCodigo(String codigo) {
        return new RouteDuplicateException("Ya existe una ruta con el código: " + codigo);
    }
}

