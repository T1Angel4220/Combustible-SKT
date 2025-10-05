package com.skt.combustible.auth.application.service;

import com.skt.combustible.auth.domain.dto.AuthResponse;
import com.skt.combustible.auth.domain.dto.LoginRequest;
import com.skt.combustible.auth.domain.dto.RegisterRequest;
import com.skt.combustible.auth.domain.entity.Usuario;
import com.skt.combustible.auth.infrastructure.repository.UsuarioRepository;
import com.skt.combustible.shared.domain.enums.RolUsuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de autenticación y autorización
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Service
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtService jwtService;
    
    /**
     * Autentica un usuario y genera un token JWT
     * 
     * @param loginRequest los datos de login
     * @return AuthResponse con el token y datos del usuario
     * @throws RuntimeException si las credenciales son inválidas
     */
    public AuthResponse login(LoginRequest loginRequest) {
        logger.info("Intentando autenticar usuario: {}", loginRequest.getUsernameOrEmail());
        
        // Buscar usuario por username o email
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsernameOrEmail(
            loginRequest.getUsernameOrEmail(), 
            loginRequest.getUsernameOrEmail()
        );
        
        if (usuarioOpt.isEmpty()) {
            logger.warn("Usuario no encontrado: {}", loginRequest.getUsernameOrEmail());
            throw new RuntimeException("Credenciales inválidas");
        }
        
        Usuario usuario = usuarioOpt.get();
        
        // Verificar si el usuario está activo
        if (!usuario.isActivo()) {
            logger.warn("Usuario inactivo intentando autenticarse: {}", usuario.getUsername());
            throw new RuntimeException("Usuario inactivo");
        }
        
        // Verificar contraseña
        if (!passwordEncoder.matches(loginRequest.getPassword(), usuario.getPassword())) {
            logger.warn("Contraseña incorrecta para usuario: {}", usuario.getUsername());
            throw new RuntimeException("Credenciales inválidas");
        }
        
        // Actualizar último acceso
        usuario.setUltimoAcceso(LocalDateTime.now());
        usuarioRepository.save(usuario);
        
        // Generar token JWT
        String token = jwtService.generateToken(usuario);
        LocalDateTime expiracion = jwtService.getExpirationDateAsLocalDateTime(token);
        
        logger.info("Usuario autenticado exitosamente: {}", usuario.getUsername());
        
        return new AuthResponse(
            token,
            usuario.getUsername(),
            usuario.getEmail(),
            usuario.getNombre(),
            usuario.getApellido(),
            usuario.getRol(),
            usuario.getPermisos(),
            expiracion
        );
    }
    
    /**
     * Registra un nuevo usuario en el sistema
     * 
     * @param registerRequest los datos de registro
     * @return AuthResponse con el token y datos del usuario
     * @throws RuntimeException si el username o email ya existen
     */
    public AuthResponse register(RegisterRequest registerRequest) {
        logger.info("Intentando registrar nuevo usuario: {}", registerRequest.getUsername());
        
        // Verificar si el username ya existe
        if (usuarioRepository.existsByUsername(registerRequest.getUsername())) {
            logger.warn("Username ya existe: {}", registerRequest.getUsername());
            throw new RuntimeException("El username ya está en uso");
        }
        
        // Verificar si el email ya existe
        if (usuarioRepository.existsByEmail(registerRequest.getEmail())) {
            logger.warn("Email ya existe: {}", registerRequest.getEmail());
            throw new RuntimeException("El email ya está en uso");
        }
        
        // Crear nuevo usuario
        Usuario usuario = new Usuario();
        usuario.setUsername(registerRequest.getUsername());
        usuario.setEmail(registerRequest.getEmail());
        usuario.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        usuario.setNombre(registerRequest.getNombre());
        usuario.setApellido(registerRequest.getApellido());
        usuario.setRol(registerRequest.getRol());
        usuario.setPermisos(registerRequest.getPermisos());
        usuario.setActivo(true);
        usuario.setFechaCreacion(LocalDateTime.now());
        
        // Guardar usuario
        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        
        // Generar token JWT
        String token = jwtService.generateToken(usuarioGuardado);
        LocalDateTime expiracion = jwtService.getExpirationDateAsLocalDateTime(token);
        
        logger.info("Usuario registrado exitosamente: {}", usuarioGuardado.getUsername());
        
        return new AuthResponse(
            token,
            usuarioGuardado.getUsername(),
            usuarioGuardado.getEmail(),
            usuarioGuardado.getNombre(),
            usuarioGuardado.getApellido(),
            usuarioGuardado.getRol(),
            usuarioGuardado.getPermisos(),
            expiracion
        );
    }
    
    /**
     * Valida un token JWT
     * 
     * @param token el token a validar
     * @return true si el token es válido, false en caso contrario
     */
    public boolean validateToken(String token) {
        return jwtService.validateToken(token);
    }
    
    /**
     * Obtiene los datos del usuario desde el token JWT
     * 
     * @param token el token JWT
     * @return AuthResponse con los datos del usuario
     * @throws RuntimeException si el token es inválido
     */
    public AuthResponse getUserFromToken(String token) {
        if (!jwtService.validateToken(token)) {
            throw new RuntimeException("Token inválido");
        }
        
        String username = jwtService.getUsernameFromToken(token);
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        
        if (usuarioOpt.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }
        
        Usuario usuario = usuarioOpt.get();
        
        return new AuthResponse(
            token,
            usuario.getUsername(),
            usuario.getEmail(),
            usuario.getNombre(),
            usuario.getApellido(),
            usuario.getRol(),
            usuario.getPermisos(),
            jwtService.getExpirationDateAsLocalDateTime(token)
        );
    }
    
    /**
     * Verifica si un usuario tiene un rol específico
     * 
     * @param token el token JWT
     * @param rol el rol a verificar
     * @return true si el usuario tiene el rol, false en caso contrario
     */
    public boolean hasRole(String token, RolUsuario rol) {
        return jwtService.hasRole(token, rol);
    }
    
    /**
     * Verifica si un usuario tiene un permiso específico
     * 
     * @param token el token JWT
     * @param permiso el permiso a verificar
     * @return true si el usuario tiene el permiso, false en caso contrario
     */
    public boolean hasPermission(String token, String permiso) {
        return jwtService.hasPermission(token, permiso);
    }
    
    /**
     * Obtiene un usuario por username
     * 
     * @param username el username del usuario
     * @return Optional con el usuario encontrado
     */
    public Optional<Usuario> getUserByUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }
    
    /**
     * Obtiene usuarios por rol
     * 
     * @param rol el rol a buscar
     * @return lista de usuarios con ese rol
     */
    public List<Usuario> getUsersByRole(RolUsuario rol) {
        return usuarioRepository.findByRolAndActivoTrue(rol);
    }
}
