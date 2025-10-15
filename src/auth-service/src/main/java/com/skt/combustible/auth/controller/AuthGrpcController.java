package com.skt.combustible.auth.controller;

import com.skt.combustible.auth.application.service.AuthService;
import com.skt.combustible.auth.application.service.JwtService;
import com.skt.combustible.auth.domain.entity.Usuario;
import com.skt.combustible.auth.grpc.*;
import com.skt.combustible.shared.domain.enums.RolUsuario;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controlador gRPC para el servicio de autenticación
 * Implementa TODAS las funcionalidades incluyendo refresh token
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@GrpcService
public class AuthGrpcController extends AuthServiceGrpc.AuthServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(AuthGrpcController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @Override
    public void login(com.skt.combustible.auth.grpc.LoginRequest request,
            StreamObserver<com.skt.combustible.auth.grpc.AuthResponse> responseObserver) {
        logger.info("gRPC: Login request para usuario: {}", request.getUsernameOrEmail());

        try {
            // Llamar al servicio de autenticación
            com.skt.combustible.auth.domain.dto.AuthResponse authResponse = authService
                    .login(request.getUsernameOrEmail(), request.getPassword());

            // Convertir a gRPC response con refresh token
            com.skt.combustible.auth.grpc.AuthResponse grpcResponse = com.skt.combustible.auth.grpc.AuthResponse
                    .newBuilder()
                    .setToken(authResponse.getToken())
                    .setRefreshToken(authResponse.getRefreshToken() != null ? authResponse.getRefreshToken() : "")
                    .setTokenType("Bearer")
                    .setExpiresIn(jwtService.getAccessTokenExpiration())
                    .setUser(mapAuthResponseToGrpcUser(authResponse))
                    .build();

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();

            logger.info("gRPC: Login exitoso para usuario: {}", request.getUsernameOrEmail());

        } catch (Exception e) {
            logger.error("Error en login gRPC: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.UNAUTHENTICATED
                    .withDescription("Credenciales inválidas: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void register(com.skt.combustible.auth.grpc.RegisterRequest request,
            StreamObserver<com.skt.combustible.auth.grpc.AuthResponse> responseObserver) {
        logger.info("gRPC: Register request para usuario: {}", request.getUsername());

        try {
            // Convertir Role de gRPC a dominio
            RolUsuario role = mapGrpcRoleToDomain(request.getRole());

            // Llamar al servicio de registro
            com.skt.combustible.auth.domain.dto.AuthResponse authResponse = authService.register(request.getUsername(),
                    request.getEmail(), request.getPassword(), role);

            // Convertir a gRPC response con refresh token
            com.skt.combustible.auth.grpc.AuthResponse grpcResponse = com.skt.combustible.auth.grpc.AuthResponse
                    .newBuilder()
                    .setToken(authResponse.getToken())
                    .setRefreshToken(authResponse.getRefreshToken() != null ? authResponse.getRefreshToken() : "")
                    .setTokenType("Bearer")
                    .setExpiresIn(jwtService.getAccessTokenExpiration())
                    .setUser(mapAuthResponseToGrpcUser(authResponse))
                    .build();

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();

            logger.info("gRPC: Registro exitoso para usuario: {}", request.getUsername());

        } catch (Exception e) {
            logger.error("Error en register gRPC: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                    .withDescription("Error en registro: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void validateToken(com.skt.combustible.auth.grpc.ValidateTokenRequest request,
            StreamObserver<com.skt.combustible.auth.grpc.ValidateTokenResponse> responseObserver) {
        logger.info("gRPC: Validate token request");

        try {
            // Validar token JWT
            boolean isValid = jwtService.validateToken(request.getToken());

            if (isValid) {
                // Obtener información del usuario del token
                String username = jwtService.getUsernameFromToken(request.getToken());
                RolUsuario role = jwtService.getRolFromToken(request.getToken());
                long expiresAt = jwtService.getExpirationDateFromToken(request.getToken()).getTime();

                com.skt.combustible.auth.grpc.ValidateTokenResponse grpcResponse = com.skt.combustible.auth.grpc.ValidateTokenResponse
                        .newBuilder()
                        .setValid(true)
                        .setUsername(username)
                        .setRole(mapDomainRoleToGrpc(role))
                        .setExpiresAt(expiresAt)
                        .build();

                responseObserver.onNext(grpcResponse);
                logger.info("gRPC: Token válido para usuario: {}", username);
            } else {
                com.skt.combustible.auth.grpc.ValidateTokenResponse grpcResponse = com.skt.combustible.auth.grpc.ValidateTokenResponse
                        .newBuilder()
                        .setValid(false)
                        .setUsername("")
                        .setRole(com.skt.combustible.auth.grpc.Role.ADMIN) // Valor por defecto
                        .setExpiresAt(0)
                        .build();

                responseObserver.onNext(grpcResponse);
                logger.warn("gRPC: Token inválido");
            }

            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("Error validando token gRPC: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.UNAUTHENTICATED
                    .withDescription("Token inválido: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getUserByToken(com.skt.combustible.auth.grpc.GetUserByTokenRequest request,
            StreamObserver<com.skt.combustible.auth.grpc.UserResponse> responseObserver) {
        logger.info("gRPC: Get user by token request");

        try {
            // Validar token y obtener usuario
            if (!jwtService.validateToken(request.getToken())) {
                responseObserver.onError(io.grpc.Status.UNAUTHENTICATED
                        .withDescription("Token inválido")
                        .asRuntimeException());
                return;
            }

            String username = jwtService.getUsernameFromToken(request.getToken());
            Usuario usuario = authService.getUserByUsername(username);

            if (usuario == null) {
                responseObserver.onError(io.grpc.Status.NOT_FOUND
                        .withDescription("Usuario no encontrado")
                        .asRuntimeException());
                return;
            }

            com.skt.combustible.auth.grpc.UserResponse grpcResponse = mapUsuarioToGrpc(usuario);
            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();

            logger.info("gRPC: Usuario obtenido exitosamente: {}", username);

        } catch (Exception e) {
            logger.error("Error obteniendo usuario por token gRPC: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error interno: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void refreshToken(com.skt.combustible.auth.grpc.RefreshTokenRequest request,
            StreamObserver<com.skt.combustible.auth.grpc.AuthResponse> responseObserver) {
        logger.info("gRPC: Refresh token request");

        try {
            // Llamar al servicio para refrescar el token
            com.skt.combustible.auth.domain.dto.AuthResponse authResponse = authService
                    .refreshToken(request.getRefreshToken());

            // Convertir a gRPC response
            com.skt.combustible.auth.grpc.AuthResponse grpcResponse = com.skt.combustible.auth.grpc.AuthResponse
                    .newBuilder()
                    .setToken(authResponse.getToken())
                    .setRefreshToken(authResponse.getRefreshToken() != null ? authResponse.getRefreshToken() : "")
                    .setTokenType("Bearer")
                    .setExpiresIn(jwtService.getAccessTokenExpiration())
                    .setUser(mapAuthResponseToGrpcUser(authResponse))
                    .build();

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();

            logger.info("gRPC: Token refrescado exitosamente");

        } catch (Exception e) {
            logger.error("Error refrescando token gRPC: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.UNAUTHENTICATED
                    .withDescription("Refresh token inválido o expirado: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void logout(com.skt.combustible.auth.grpc.LogoutRequest request,
            StreamObserver<com.skt.combustible.auth.grpc.Empty> responseObserver) {
        logger.info("gRPC: Logout request");

        try {
            // En un sistema stateless como JWT, el logout se maneja en el cliente
            // El cliente simplemente elimina los tokens almacenados
            // Aquí solo confirmamos la operación

            com.skt.combustible.auth.grpc.Empty emptyResponse = com.skt.combustible.auth.grpc.Empty.newBuilder()
                    .build();

            responseObserver.onNext(emptyResponse);
            responseObserver.onCompleted();

            logger.info("gRPC: Logout procesado exitosamente");

        } catch (Exception e) {
            logger.error("Error en logout gRPC: {}", e.getMessage(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error interno: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    // ==================== MÉTODOS DE MAPEO ====================

    /**
     * Convierte AuthResponse del dominio a UserResponse de gRPC
     */
    private com.skt.combustible.auth.grpc.UserResponse mapAuthResponseToGrpcUser(
            com.skt.combustible.auth.domain.dto.AuthResponse authResponse) {
        return com.skt.combustible.auth.grpc.UserResponse.newBuilder()
                .setId("") // No disponible en AuthResponse
                .setUsername(authResponse.getUsername())
                .setEmail(authResponse.getEmail())
                .setRole(mapDomainRoleToGrpc(authResponse.getRol()))
                .setActive(true) // Asumir activo si se autenticó
                .setCreatedAt("") // No disponible en AuthResponse
                .setUpdatedAt("") // No disponible en AuthResponse
                .build();
    }

    /**
     * Convierte Usuario del dominio a UserResponse de gRPC
     */
    private com.skt.combustible.auth.grpc.UserResponse mapUsuarioToGrpc(Usuario usuario) {
        return com.skt.combustible.auth.grpc.UserResponse.newBuilder()
                .setId(usuario.getId())
                .setUsername(usuario.getUsername())
                .setEmail(usuario.getEmail())
                .setRole(mapDomainRoleToGrpc(usuario.getRol()))
                .setActive(usuario.isActivo())
                .setCreatedAt(
                        usuario.getFechaCreacion() != null ? usuario.getFechaCreacion().format(DATE_FORMATTER) : "")
                .setUpdatedAt(usuario.getUltimoAcceso() != null ? usuario.getUltimoAcceso().format(DATE_FORMATTER) : "")
                .build();
    }

    /**
     * Convierte RolUsuario del dominio a Role de gRPC
     */
    private com.skt.combustible.auth.grpc.Role mapDomainRoleToGrpc(RolUsuario role) {
        return switch (role) {
            case ADMIN -> com.skt.combustible.auth.grpc.Role.ADMIN;
            case OPERADOR -> com.skt.combustible.auth.grpc.Role.OPERADOR;
            case SUPERVISOR -> com.skt.combustible.auth.grpc.Role.SUPERVISOR;
        };
    }

    /**
     * Convierte Role de gRPC a RolUsuario del dominio
     */
    private RolUsuario mapGrpcRoleToDomain(com.skt.combustible.auth.grpc.Role grpcRole) {
        return switch (grpcRole) {
            case ADMIN -> RolUsuario.ADMIN;
            case OPERADOR -> RolUsuario.OPERADOR;
            case SUPERVISOR -> RolUsuario.SUPERVISOR;
            default -> RolUsuario.OPERADOR; // Valor por defecto
        };
    }
}