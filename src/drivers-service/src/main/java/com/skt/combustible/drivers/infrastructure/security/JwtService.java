package com.skt.combustible.drivers.infrastructure.security;

import com.skt.combustible.shared.domain.enums.RolUsuario;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

/**
 * Servicio para validación de JWT tokens en Drivers Service
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
}
