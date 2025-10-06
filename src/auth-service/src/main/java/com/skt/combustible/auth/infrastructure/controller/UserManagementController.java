package com.skt.combustible.auth.infrastructure.controller;

import com.skt.combustible.auth.application.service.AuthService;
import com.skt.combustible.auth.domain.dto.RegisterRequest;
import com.skt.combustible.auth.domain.entity.Usuario;
import com.skt.combustible.shared.domain.enums.RolUsuario;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador para gestión de usuarios (solo administradores)
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserManagementController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserManagementController.class);
    
    @Autowired
    private AuthService authService;
    
    /**
     * Obtiene todos los usuarios (solo administradores)
     * 
     * @param authorization header de autorización
     * @return ResponseEntity con lista de usuarios
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers(@RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.replace("Bearer ", "");
            
            if (!authService.hasRole(token, RolUsuario.ADMIN)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Acceso denegado");
                error.put("message", "Solo los administradores pueden acceder a esta funcionalidad");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            // Por simplicidad, obtenemos usuarios por rol
            List<Usuario> admins = authService.getUsersByRole(RolUsuario.ADMIN);
            List<Usuario> supervisores = authService.getUsersByRole(RolUsuario.SUPERVISOR);
            List<Usuario> operadores = authService.getUsersByRole(RolUsuario.OPERADOR);
            
            Map<String, Object> response = new HashMap<>();
            response.put("administradores", admins);
            response.put("supervisores", supervisores);
            response.put("operadores", operadores);
            
            logger.info("Lista de usuarios obtenida por administrador");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error obteniendo usuarios: ", e);
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Obtiene un usuario por username (solo administradores)
     * 
     * @param username el username del usuario
     * @param authorization header de autorización
     * @return ResponseEntity con el usuario encontrado
     */
    @GetMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username, 
                                              @RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.replace("Bearer ", "");
            
            if (!authService.hasRole(token, RolUsuario.ADMIN)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Acceso denegado");
                error.put("message", "Solo los administradores pueden acceder a esta funcionalidad");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            Optional<Usuario> usuarioOpt = authService.getUserByUsername(username);
            
            if (usuarioOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Usuario no encontrado");
                error.put("message", "No existe un usuario con el username: " + username);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            Usuario usuario = usuarioOpt.get();
            // No devolver la contraseña
            usuario.setPassword(null);
            
            logger.info("Usuario obtenido por administrador: {}", username);
            return ResponseEntity.ok(usuario);
            
        } catch (Exception e) {
            logger.error("Error obteniendo usuario: ", e);
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Crea un nuevo usuario (solo administradores)
     * 
     * @param registerRequest datos del nuevo usuario
     * @param authorization header de autorización
     * @return ResponseEntity con el usuario creado
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@Valid @RequestBody RegisterRequest registerRequest,
                                         @RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.replace("Bearer ", "");
            
            if (!authService.hasRole(token, RolUsuario.ADMIN)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Acceso denegado");
                error.put("message", "Solo los administradores pueden crear usuarios");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            // Usar el servicio de registro existente
            var authResponse = authService.register(registerRequest);
            
            logger.info("Usuario creado por administrador: {}", registerRequest.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
            
        } catch (RuntimeException e) {
            logger.error("Error creando usuario: {}", e.getMessage());
            
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            logger.error("Error interno creando usuario: ", e);
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Obtiene estadísticas de usuarios (solo administradores)
     * 
     * @param authorization header de autorización
     * @return ResponseEntity con estadísticas
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserStats(@RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.replace("Bearer ", "");
            
            if (!authService.hasRole(token, RolUsuario.ADMIN)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Acceso denegado");
                error.put("message", "Solo los administradores pueden acceder a esta funcionalidad");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            List<Usuario> admins = authService.getUsersByRole(RolUsuario.ADMIN);
            List<Usuario> supervisores = authService.getUsersByRole(RolUsuario.SUPERVISOR);
            List<Usuario> operadores = authService.getUsersByRole(RolUsuario.OPERADOR);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsuarios", admins.size() + supervisores.size() + operadores.size());
            stats.put("administradores", admins.size());
            stats.put("supervisores", supervisores.size());
            stats.put("operadores", operadores.size());
            
            logger.info("Estadísticas de usuarios obtenidas por administrador");
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            logger.error("Error obteniendo estadísticas: ", e);
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
