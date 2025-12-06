package com.skt.combustible.routes.infrastructure.security;

/**
 * Clase helper para almacenar el token JWT en ThreadLocal
 * Permite que el interceptor gRPC acceda al token sin depender del RequestContextHolder
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
public class JwtTokenHolder {
    
    private static final ThreadLocal<String> tokenHolder = new ThreadLocal<>();

    /**
     * Almacena el token JWT en el ThreadLocal
     * 
     * @param token el token JWT a almacenar
     */
    public static void setToken(String token) {
        tokenHolder.set(token);
    }

    /**
     * Obtiene el token JWT del ThreadLocal
     * 
     * @return el token JWT o null si no está disponible
     */
    public static String getToken() {
        return tokenHolder.get();
    }

    /**
     * Limpia el token del ThreadLocal
     * Debe llamarse después de procesar la solicitud para evitar memory leaks
     */
    public static void clear() {
        tokenHolder.remove();
    }
}

