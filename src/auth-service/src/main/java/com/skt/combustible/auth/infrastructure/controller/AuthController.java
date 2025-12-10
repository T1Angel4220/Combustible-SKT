package com.skt.combustible.auth.infrastructure.controller;

import com.skt.combustible.auth.application.service.AuthService;
import com.skt.combustible.auth.domain.dto.AuthResponse;
import com.skt.combustible.auth.domain.dto.LoginRequest;
import com.skt.combustible.auth.domain.dto.RegisterRequest;
import com.skt.combustible.shared.domain.enums.RolUsuario;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para autenticación y autorización
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthService authService;
    
    /**
     * Endpoint para login de usuarios
     * 
     * @param loginRequest datos de login
     * @return ResponseEntity con AuthResponse o error
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, 
                                    jakarta.servlet.http.HttpServletRequest request) {
        try {
            logger.info("========================================");
            logger.info("Solicitud de login recibida para: {}", loginRequest.getUsernameOrEmail());
            logger.info("Remote Address: {}", request.getRemoteAddr());
            logger.info("Request URL: {}", request.getRequestURL());
            logger.info("Request Method: {}", request.getMethod());
            logger.info("Origin Header: {}", request.getHeader("Origin"));
            logger.info("========================================");
            
            AuthResponse response = authService.login(loginRequest);
            
            // Crear respuesta en formato esperado por el frontend
            Map<String, Object> loginResponse = new HashMap<>();
            loginResponse.put("token", response.getToken());
            loginResponse.put("user", response);
            
            logger.info("Login exitoso para usuario: {}", response.getUsername());
            return ResponseEntity.ok(loginResponse);
            
        } catch (RuntimeException e) {
            logger.error("Error en login: {}", e.getMessage());
            
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("message", "Credenciales inválidas o usuario inactivo");
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            logger.error("Error interno en login: ", e);
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
            error.put("message", "Ocurrió un error inesperado");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Endpoint para registro de nuevos usuarios
     * 
     * @param registerRequest datos de registro
     * @return ResponseEntity con AuthResponse o error
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            logger.info("Solicitud de registro recibida para: {}", registerRequest.getUsername());
            
            AuthResponse response = authService.register(registerRequest);
            
            // Crear respuesta en formato esperado por el frontend
            Map<String, Object> registerResponse = new HashMap<>();
            registerResponse.put("token", response.getToken());
            registerResponse.put("user", response);
            
            logger.info("Registro exitoso para usuario: {}", response.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(registerResponse);
            
        } catch (RuntimeException e) {
            logger.error("Error en registro: {}", e.getMessage());
            
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("message", "No se pudo completar el registro");
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            logger.error("Error interno en registro: ", e);
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
            error.put("message", "Ocurrió un error inesperado");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Endpoint para validar un token JWT
     * 
     * @param tokenRequest objeto con el token a validar
     * @return ResponseEntity con información del token o error
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestBody Map<String, String> tokenRequest) {
        try {
            logger.info("Solicitud de validación de token recibida");
            
            String token = tokenRequest.get("token");
            if (token == null || token.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("valid", "false");
                error.put("error", "Token no proporcionado");
                
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            if (authService.validateToken(token)) {
                AuthResponse userInfo = authService.getUserFromToken(token);
                
                Map<String, Object> response = new HashMap<>();
                response.put("valid", true);
                response.put("user", userInfo);
                
                logger.info("Token válido para usuario: {}", userInfo.getUsername());
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("valid", "false");
                error.put("error", "Token inválido");
                
                logger.warn("Token inválido recibido");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
        } catch (Exception e) {
            logger.error("Error en validación de token: ", e);
            
            Map<String, String> error = new HashMap<>();
            error.put("valid", "false");
            error.put("error", "Error interno del servidor");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Endpoint para obtener información del usuario desde el token
     * 
     * @param authorization header de autorización con Bearer token
     * @return ResponseEntity con información del usuario o error
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.replace("Bearer ", "");
            
            AuthResponse userInfo = authService.getUserFromToken(token);
            
            logger.info("Información de usuario obtenida para: {}", userInfo.getUsername());
            return ResponseEntity.ok(userInfo);
            
        } catch (RuntimeException e) {
            logger.error("Error obteniendo información del usuario: {}", e.getMessage());
            
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            logger.error("Error interno obteniendo información del usuario: ", e);
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Endpoint para verificar si un usuario tiene un rol específico
     * 
     * @param authorization header de autorización con Bearer token
     * @param rol el rol a verificar
     * @return ResponseEntity con resultado de la verificación
     */
    @GetMapping("/has-role/{rol}")
    public ResponseEntity<?> hasRole(@RequestHeader("Authorization") String authorization, 
                                    @PathVariable String rol) {
        try {
            String token = authorization.replace("Bearer ", "");
            
            RolUsuario rolUsuario = RolUsuario.valueOf(rol.toUpperCase());
            boolean hasRole = authService.hasRole(token, rolUsuario);
            
            Map<String, Object> response = new HashMap<>();
            response.put("hasRole", hasRole);
            response.put("role", rol);
            
            logger.info("Verificación de rol {}: {}", rol, hasRole);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Rol inválido: {}", rol);
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Rol inválido: " + rol);
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            logger.error("Error verificando rol: ", e);
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Endpoint para verificar si un usuario tiene un permiso específico
     * 
     * @param authorization header de autorización con Bearer token
     * @param permiso el permiso a verificar
     * @return ResponseEntity con resultado de la verificación
     */
    @GetMapping("/has-permission/{permiso}")
    public ResponseEntity<?> hasPermission(@RequestHeader("Authorization") String authorization, 
                                          @PathVariable String permiso) {
        try {
            String token = authorization.replace("Bearer ", "");
            
            boolean hasPermission = authService.hasPermission(token, permiso);
            
            Map<String, Object> response = new HashMap<>();
            response.put("hasPermission", hasPermission);
            response.put("permission", permiso);
            
            logger.info("Verificación de permiso {}: {}", permiso, hasPermission);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error verificando permiso: ", e);
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
