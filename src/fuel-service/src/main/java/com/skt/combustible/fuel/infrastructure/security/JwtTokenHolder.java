package com.skt.combustible.fuel.infrastructure.security;

/**
 * Clase helper para almacenar el token JWT en ThreadLocal para acceso en interceptores gRPC
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
public class JwtTokenHolder {
    private static final ThreadLocal<String> tokenHolder = new ThreadLocal<>();
    
    public static void setToken(String token) {
        tokenHolder.set(token);
    }
    
    public static String getToken() {
        return tokenHolder.get();
    }
    
    public static void clear() {
        tokenHolder.remove();
    }
}

