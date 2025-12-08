package com.skt.combustible.routes.infrastructure.security;

import com.skt.combustible.shared.domain.enums.RolUsuario;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Servicio para validación de JWT tokens en Routes Service
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * Valida un token JWT
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
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Extrae el rol del usuario del token JWT
     * 
     * @param token el token JWT
     * @return el rol del usuario extraído del token
     * @throws IllegalArgumentException si el rol no existe o no es válido
     */
    public RolUsuario getRolFromToken(String token) {
        String rolString = getClaimFromToken(token, claims -> claims.get("rol", String.class));
        if (rolString == null || rolString.trim().isEmpty()) {
            logger.error("El claim 'rol' no existe o está vacío en el token JWT");
            throw new IllegalArgumentException("El claim 'rol' no existe o está vacío en el token JWT");
        }
        try {
            return RolUsuario.valueOf(rolString);
        } catch (IllegalArgumentException e) {
            logger.error("Rol '{}' no es un valor válido del enum RolUsuario", rolString);
            throw new IllegalArgumentException("Rol '" + rolString + "' no es válido", e);
        }
    }

    /**
     * Extrae el ID del usuario del token JWT
     * El userId puede estar en "sub" o "userId" según la versión del token
     */
    public String getUserIdFromToken(String token) {
        try {
            // Primero intentar obtener de "sub" (estándar JWT)
            String userId = getClaimFromToken(token, claims -> claims.get("sub", String.class));
            if (userId != null && !userId.isEmpty()) {
                return userId;
            }
            // Si no está en "sub", intentar "userId"
            return getClaimFromToken(token, claims -> claims.get("userId", String.class));
        } catch (Exception e) {
            logger.error("Error extrayendo userId del token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Verifica si el token está expirado
     */
    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * Obtiene la fecha de expiración del token
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Extrae un claim específico del token JWT
     */
    public <T> T getClaimFromToken(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Obtiene todos los claims del token JWT
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
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Verifica si el usuario tiene un rol específico según el token
     */
    public boolean hasRole(String token, RolUsuario rol) {
        RolUsuario userRole = getRolFromToken(token);
        return userRole == rol;
    }
}

