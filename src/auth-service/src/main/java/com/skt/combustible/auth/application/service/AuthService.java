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
                loginRequest.getUsernameOrEmail());

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

        // Generar tokens JWT
        String token = jwtService.generateToken(usuario);
        String refreshToken = jwtService.generateRefreshToken(usuario);
        LocalDateTime expiracion = jwtService.getExpirationDateAsLocalDateTime(token);

        logger.info("Usuario autenticado exitosamente: {}", usuario.getUsername());

        AuthResponse authResponse = new AuthResponse(
                token,
                usuario.getId(),
                usuario.getUsername(),
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getRol(),
                usuario.getPermisos(),
                expiracion);
        authResponse.setRefreshToken(refreshToken); // Agregar refresh token

        return authResponse;
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
                usuarioGuardado.getId(),
                usuarioGuardado.getUsername(),
                usuarioGuardado.getEmail(),
                usuarioGuardado.getNombre(),
                usuarioGuardado.getApellido(),
                usuarioGuardado.getRol(),
                usuarioGuardado.getPermisos(),
                expiracion);
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
                usuario.getId(),
                usuario.getUsername(),
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getRol(),
                usuario.getPermisos(),
                jwtService.getExpirationDateAsLocalDateTime(token));
    }

    /**
     * Verifica si un usuario tiene un rol específico
     * 
     * @param token el token JWT
     * @param rol   el rol a verificar
     * @return true si el usuario tiene el rol, false en caso contrario
     */
    public boolean hasRole(String token, RolUsuario rol) {
        return jwtService.hasRole(token, rol);
    }

    /**
     * Verifica si un usuario tiene un permiso específico
     * 
     * @param token   el token JWT
     * @param permiso el permiso a verificar
     * @return true si el usuario tiene el permiso, false en caso contrario
     */
    public boolean hasPermission(String token, String permiso) {
        return jwtService.hasPermission(token, permiso);
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

    /**
     * Login sobrecargado para llamadas gRPC
     * 
     * @param usernameOrEmail username o email
     * @param password        contraseña
     * @return AuthResponse con token y refresh token
     */
    public AuthResponse login(String usernameOrEmail, String password) {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsernameOrEmail(usernameOrEmail);
        loginRequest.setPassword(password);
        return login(loginRequest);
    }

    /**
     * Register sobrecargado para llamadas gRPC
     * 
     * @param username el username
     * @param email    el email
     * @param password la contraseña
     * @param rol      el rol del usuario
     * @return AuthResponse con token y refresh token
     */
    public AuthResponse register(String username, String email, String password, RolUsuario rol) {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername(username);
        registerRequest.setEmail(email);
        registerRequest.setPassword(password);
        registerRequest.setRol(rol);
        registerRequest.setNombre(username); // Nombre por defecto
        registerRequest.setApellido(""); // Apellido vacío por defecto
        registerRequest.setPermisos(List.of()); // Permisos vacíos por defecto
        return register(registerRequest);
    }

    /**
     * Refresca un access token usando un refresh token válido
     * 
     * @param refreshToken el refresh token
     * @return AuthResponse con nuevo access token
     * @throws RuntimeException si el refresh token es inválido
     */
    public AuthResponse refreshToken(String refreshToken) {
        logger.info("Intentando refrescar token");

        // Validar refresh token
        if (!jwtService.validateRefreshToken(refreshToken)) {
            logger.warn("Refresh token inválido");
            throw new RuntimeException("Refresh token inválido o expirado");
        }

        // Obtener username del refresh token
        String username = jwtService.getUsernameFromToken(refreshToken);
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);

        if (usuarioOpt.isEmpty()) {
            logger.warn("Usuario no encontrado para refresh token: {}", username);
            throw new RuntimeException("Usuario no encontrado");
        }

        Usuario usuario = usuarioOpt.get();

        // Verificar si el usuario está activo
        if (!usuario.isActivo()) {
            logger.warn("Usuario inactivo intentando refrescar token: {}", usuario.getUsername());
            throw new RuntimeException("Usuario inactivo");
        }

        // Generar nuevo access token y refresh token
        String newAccessToken = jwtService.generateToken(usuario);
        String newRefreshToken = jwtService.generateRefreshToken(usuario);
        LocalDateTime expiracion = jwtService.getExpirationDateAsLocalDateTime(newAccessToken);

        logger.info("Token refrescado exitosamente para usuario: {}", usuario.getUsername());

        AuthResponse authResponse = new AuthResponse(
                newAccessToken,
                usuario.getId(),
                usuario.getUsername(),
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getRol(),
                usuario.getPermisos(),
                expiracion);
        authResponse.setRefreshToken(newRefreshToken);

        return authResponse;
    }

    /**
     * Obtiene un usuario por su username (para uso interno)
     * 
     * @param username el username
     * @return el usuario encontrado
     */
    public Usuario getUserByUsername(String username) {
        return usuarioRepository.findByUsername(username).orElse(null);
    }
}
