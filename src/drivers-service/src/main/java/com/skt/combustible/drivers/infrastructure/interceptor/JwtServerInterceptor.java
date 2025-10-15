package com.skt.combustible.drivers.infrastructure.interceptor;

import io.grpc.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

/**
 * Interceptor gRPC del servidor para validar tokens JWT
 * Se ejecuta antes de cada llamada gRPC para verificar la autenticación
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@Component
@GrpcGlobalServerInterceptor
public class JwtServerInterceptor implements ServerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JwtServerInterceptor.class);
    private static final Metadata.Key<String> AUTHORIZATION_METADATA_KEY = Metadata.Key.of("Authorization",
            Metadata.ASCII_STRING_MARSHALLER);

    // Context key para almacenar información del usuario autenticado
    public static final Context.Key<String> USER_ID_CONTEXT_KEY = Context.key("userId");
    public static final Context.Key<String> USERNAME_CONTEXT_KEY = Context.key("username");
    public static final Context.Key<String> USER_ROLE_CONTEXT_KEY = Context.key("userRole");

    @Value("${jwt.secret:mySecretKey1234567890123456789012345}")
    private String jwtSecret;

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String methodName = call.getMethodDescriptor().getFullMethodName();
        logger.debug("Interceptando llamada gRPC: {}", methodName);

        // Permitir métodos de health check y reflection sin autenticación
        if (isPublicMethod(methodName)) {
            logger.debug("Método público, permitiendo sin autenticación: {}", methodName);
            return next.startCall(call, headers);
        }

        try {
            // Obtener el token JWT del header
            String authHeader = headers.get(AUTHORIZATION_METADATA_KEY);

            if (authHeader == null || authHeader.isEmpty()) {
                logger.warn("No se encontró token JWT en la llamada gRPC: {}", methodName);
                call.close(Status.UNAUTHENTICATED
                        .withDescription("No se encontró token de autenticación"),
                        new Metadata());
                return new ServerCall.Listener<ReqT>() {
                };
            }

            // Extraer el token (remover "Bearer " si está presente)
            String token = authHeader.startsWith("Bearer ")
                    ? authHeader.substring(7)
                    : authHeader;

            // Validar el token JWT
            Claims claims = validateToken(token);

            if (claims == null) {
                logger.warn("Token JWT inválido para la llamada gRPC: {}", methodName);
                call.close(Status.UNAUTHENTICATED
                        .withDescription("Token de autenticación inválido"),
                        new Metadata());
                return new ServerCall.Listener<ReqT>() {
                };
            }

            // Extraer información del usuario del token
            String userId = claims.get("userId", String.class);
            String username = claims.getSubject();
            String userRole = claims.get("rol", String.class);

            logger.info("Usuario autenticado: {} (ID: {}, Rol: {}) - Método: {}",
                    username, userId, userRole, methodName);

            // Crear un contexto con la información del usuario
            Context context = Context.current()
                    .withValue(USER_ID_CONTEXT_KEY, userId)
                    .withValue(USERNAME_CONTEXT_KEY, username)
                    .withValue(USER_ROLE_CONTEXT_KEY, userRole);

            // Continuar con la llamada en el contexto autenticado
            return Contexts.interceptCall(context, call, headers, next);

        } catch (Exception e) {
            logger.error("Error validando token JWT en gRPC: {}", e.getMessage());
            call.close(Status.INTERNAL
                    .withDescription("Error validando autenticación: " + e.getMessage()),
                    new Metadata());
            return new ServerCall.Listener<ReqT>() {
            };
        }
    }

    /**
     * Valida el token JWT y retorna los claims
     */
    private Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            logger.error("Error validando token JWT: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene la clave de firma para JWT
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Determina si un método es público (no requiere autenticación)
     */
    private boolean isPublicMethod(String methodName) {
        // Permitir health checks y reflection
        return methodName.contains("grpc.health") ||
                methodName.contains("grpc.reflection") ||
                methodName.contains("ServerReflection");
    }

    /**
     * Obtiene el ID del usuario del contexto actual
     */
    public static String getCurrentUserId() {
        return USER_ID_CONTEXT_KEY.get();
    }

    /**
     * Obtiene el username del contexto actual
     */
    public static String getCurrentUsername() {
        return USERNAME_CONTEXT_KEY.get();
    }

    /**
     * Obtiene el rol del usuario del contexto actual
     */
    public static String getCurrentUserRole() {
        return USER_ROLE_CONTEXT_KEY.get();
    }
}
