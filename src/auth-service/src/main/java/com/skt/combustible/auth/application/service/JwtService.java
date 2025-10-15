package com.skt.combustible.auth.application.service;

import com.skt.combustible.auth.domain.entity.Usuario;
import com.skt.combustible.shared.domain.enums.RolUsuario;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio para manejo de JWT tokens
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    @Value("${jwt.refresh-expiration:604800000}") // 7 días por defecto
    private long refreshTokenExpirationMs;

    /**
     * Genera un token JWT para el usuario
     * 
     * @param usuario el usuario para el cual generar el token
     * @return el token JWT generado
     */
    public String generateToken(Usuario usuario) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", usuario.getId());
        claims.put("username", usuario.getUsername());
        claims.put("email", usuario.getEmail());
        claims.put("rol", usuario.getRol().name());
        claims.put("permisos", usuario.getPermisos());
        claims.put("activo", usuario.isActivo());

        return createToken(claims, usuario.getUsername());
    }

    /**
     * Crea un token JWT con los claims especificados
     * 
     * @param claims  los claims del token
     * @param subject el subject del token
     * @return el token JWT creado
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Valida un token JWT
     * 
     * @param token el token a validar
     * @return true si el token es válido, false en caso contrario
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("Token JWT inválido: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extrae el username del token JWT
     * 
     * @param token el token JWT
     * @return el username extraído del token
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Extrae el ID del usuario del token JWT
     * 
     * @param token el token JWT
     * @return el ID del usuario extraído del token
     */
    public String getUserIdFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("sub", String.class));
    }

    /**
     * Extrae el rol del usuario del token JWT
     * 
     * @param token el token JWT
     * @return el rol del usuario extraído del token
     */
    public RolUsuario getRolFromToken(String token) {
        String rolString = getClaimFromToken(token, claims -> claims.get("rol", String.class));
        return RolUsuario.valueOf(rolString);
    }

    /**
     * Extrae los permisos del usuario del token JWT
     * 
     * @param token el token JWT
     * @return la lista de permisos extraída del token
     */
    @SuppressWarnings("unchecked")
    public List<String> getPermisosFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("permisos", List.class));
    }

    /**
     * Verifica si el token está expirado
     * 
     * @param token el token JWT
     * @return true si el token está expirado, false en caso contrario
     */
    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * Obtiene la fecha de expiración del token
     * 
     * @param token el token JWT
     * @return la fecha de expiración del token
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Obtiene la fecha de expiración como LocalDateTime
     * 
     * @param token el token JWT
     * @return la fecha de expiración como LocalDateTime
     */
    public LocalDateTime getExpirationDateAsLocalDateTime(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Extrae un claim específico del token JWT
     * 
     * @param token          el token JWT
     * @param claimsResolver función para extraer el claim
     * @param <T>            el tipo del claim
     * @return el claim extraído
     */
    public <T> T getClaimFromToken(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Obtiene todos los claims del token JWT
     * 
     * @param token el token JWT
     * @return todos los claims del token
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Obtiene la clave de firma para JWT
     * 
     * @return la clave de firma
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Verifica si el usuario tiene un rol específico según el token
     * 
     * @param token el token JWT
     * @param rol   el rol a verificar
     * @return true si el usuario tiene el rol, false en caso contrario
     */
    public boolean hasRole(String token, RolUsuario rol) {
        RolUsuario userRole = getRolFromToken(token);
        return userRole == rol;
    }

    /**
     * Verifica si el usuario tiene un permiso específico según el token
     * 
     * @param token   el token JWT
     * @param permiso el permiso a verificar
     * @return true si el usuario tiene el permiso, false en caso contrario
     */
    public boolean hasPermission(String token, String permiso) {
        List<String> permisos = getPermisosFromToken(token);
        return permisos != null && permisos.contains(permiso);
    }

    /**
     * Genera un refresh token para el usuario
     * 
     * @param usuario el usuario para el cual generar el refresh token
     * @return el refresh token JWT generado
     */
    public String generateRefreshToken(Usuario usuario) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", usuario.getId());
        claims.put("username", usuario.getUsername());
        claims.put("type", "refresh"); // Identificar como refresh token

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpirationMs);

        return Jwts.builder()
                .claims(claims)
                .subject(usuario.getUsername())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Verifica si un token es un refresh token
     * 
     * @param token el token a verificar
     * @return true si es un refresh token, false en caso contrario
     */
    public boolean isRefreshToken(String token) {
        try {
            String type = getClaimFromToken(token, claims -> claims.get("type", String.class));
            return "refresh".equals(type);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Valida un refresh token
     * 
     * @param refreshToken el refresh token a validar
     * @return true si el refresh token es válido, false en caso contrario
     */
    public boolean validateRefreshToken(String refreshToken) {
        return validateToken(refreshToken) && isRefreshToken(refreshToken);
    }

    /**
     * Genera un nuevo access token a partir de un refresh token válido
     * 
     * @param refreshToken el refresh token
     * @param usuario      el usuario actualizado
     * @return el nuevo access token
     */
    public String refreshAccessToken(String refreshToken, Usuario usuario) {
        if (!validateRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Refresh token inválido");
        }
        return generateToken(usuario);
    }

    /**
     * Obtiene el tiempo de expiración del access token en milisegundos
     * 
     * @return tiempo de expiración en ms
     */
    public long getAccessTokenExpiration() {
        return jwtExpirationMs;
    }

    /**
     * Obtiene el tiempo de expiración del refresh token en milisegundos
     * 
     * @return tiempo de expiración en ms
     */
    public long getRefreshTokenExpiration() {
        return refreshTokenExpirationMs;
    }
}
