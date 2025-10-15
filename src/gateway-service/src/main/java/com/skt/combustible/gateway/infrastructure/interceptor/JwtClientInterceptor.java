package com.skt.combustible.gateway.infrastructure.interceptor;

import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Interceptor gRPC para inyectar tokens JWT en las llamadas a microservicios
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@Component
public class JwtClientInterceptor implements ClientInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JwtClientInterceptor.class);
    private static final Metadata.Key<String> AUTHORIZATION_METADATA_KEY = Metadata.Key.of("Authorization",
            Metadata.ASCII_STRING_MARSHALLER);

    // ThreadLocal para almacenar el token JWT actual
    private static final ThreadLocal<String> JWT_TOKEN = new ThreadLocal<>();

    /**
     * Establece el token JWT para la siguiente llamada gRPC
     * 
     * @param token el token JWT (con o sin prefijo "Bearer ")
     */
    public static void setJwtToken(String token) {
        if (token != null && !token.isEmpty()) {
            // Normalizar el token (asegurar que tenga "Bearer " si no lo tiene)
            if (!token.startsWith("Bearer ")) {
                token = "Bearer " + token;
            }
            JWT_TOKEN.set(token);
            logger.debug("JWT token establecido para gRPC call");
        }
    }

    /**
     * Limpia el token JWT después de la llamada
     */
    public static void clearJwtToken() {
        JWT_TOKEN.remove();
        logger.debug("JWT token limpiado");
    }

    /**
     * Obtiene el token JWT actual
     * 
     * @return el token JWT o null si no hay ninguno
     */
    public static String getJwtToken() {
        return JWT_TOKEN.get();
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next) {

        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
                next.newCall(method, callOptions)) {

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                // Obtener el token JWT del ThreadLocal
                String token = JWT_TOKEN.get();

                if (token != null && !token.isEmpty()) {
                    // Agregar el header de Authorization con el token JWT
                    headers.put(AUTHORIZATION_METADATA_KEY, token);
                    logger.debug("JWT token agregado a metadata gRPC: {}",
                            token.substring(0, Math.min(20, token.length())) + "...");
                } else {
                    logger.warn("No hay JWT token disponible para la llamada gRPC");
                }

                super.start(responseListener, headers);
            }
        };
    }
}
