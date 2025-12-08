package com.skt.combustible.fuel.infrastructure.security;

import com.skt.combustible.shared.domain.enums.RolUsuario;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Filtro JWT para autenticación en Fuel Service
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    @Autowired
    private JwtService jwtService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        
        // Saltar el filtro para endpoints públicos
        if (isPublicEndpoint(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            // Validar que el token no sea null, undefined o vacío
            if (token == null || token.trim().isEmpty() || token.equals("null") || token.equals("undefined")) {
                logger.warn("Token JWT inválido en request a: {} - Token vacío o inválido", requestURI);
                sendUnauthorizedResponse(response);
                return;
            }
            
            JwtTokenHolder.setToken(token); // Almacenar token para gRPC
            
            try {
                if (jwtService.validateToken(token)) {
                    try {
                        String username = jwtService.getUsernameFromToken(token);
                        String rol = jwtService.getRolFromToken(token).name();
                        
                        // Crear autenticación
                        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                                new SimpleGrantedAuthority("ROLE_" + rol));
                        
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                username, null, authorities);
                        
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        
                        logger.debug("Usuario autenticado: {} con rol: {}", username, rol);
                    } catch (IllegalArgumentException e) {
                        logger.error("Error extrayendo rol del token JWT: {}", e.getMessage());
                        sendUnauthorizedResponse(response, "Token JWT inválido: rol no válido");
                        return;
                    } catch (NullPointerException e) {
                        logger.error("Error: rol no encontrado en token JWT: {}", e.getMessage());
                        sendUnauthorizedResponse(response, "Token JWT inválido: rol no encontrado");
                        return;
                    }
                } else {
                    logger.warn("Token JWT inválido en request a: {}", requestURI);
                    sendUnauthorizedResponse(response, "Token JWT inválido o expirado");
                    return;
                }
            } catch (io.jsonwebtoken.JwtException e) {
                logger.error("Error de JWT procesando token: {}", e.getMessage());
                sendUnauthorizedResponse(response, "Token JWT inválido: " + e.getMessage());
                return;
            } catch (Exception e) {
                logger.error("Error inesperado procesando token JWT: ", e);
                sendUnauthorizedResponse(response, "Error procesando token JWT");
                return;
            } finally {
                // No limpiar aquí, se limpiará al final del request
            }
        } else {
            // Si no hay token pero el endpoint requiere autenticación, verificar si es una solicitud de navegador
            // Para archivos HTML, CSS, JS, permitir el acceso (el frontend manejará la autenticación)
            if (requestURI.endsWith(".html") || requestURI.endsWith(".css") || requestURI.endsWith(".js") || requestURI.endsWith(".ico")) {
                logger.debug("Permitiendo acceso a archivo estático sin token: {}", requestURI);
                filterChain.doFilter(request, response);
                return;
            }
            
            logger.debug("No se encontró token JWT en request a: {}", requestURI);
            sendUnauthorizedResponse(response, "Token JWT no encontrado en la solicitud");
            return;
        }
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            JwtTokenHolder.clear(); // Limpiar token después del request
        }
    }
    
    /**
     * Verifica si el endpoint es público (no requiere autenticación)
     */
    private boolean isPublicEndpoint(String requestURI) {
        return requestURI.startsWith("/actuator/") ||
                requestURI.startsWith("/api/v1/fuel/health") ||
                requestURI.startsWith("/api/v1/fuel/info") ||
                requestURI.startsWith("/swagger-ui/") ||
                requestURI.startsWith("/v3/api-docs/") ||
                requestURI.equals("/") ||
                requestURI.endsWith(".html") ||
                requestURI.endsWith(".css") ||
                requestURI.endsWith(".js") ||
                requestURI.endsWith(".ico") ||
                requestURI.startsWith("/static/");
    }
    
    /**
     * Envía respuesta de no autorizado
     */
    private void sendUnauthorizedResponse(HttpServletResponse response) throws IOException {
        sendUnauthorizedResponse(response, "Debes iniciar sesión para acceder a la gestión de combustible");
    }

    /**
     * Envía respuesta de no autorizado con mensaje personalizado
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String jsonResponse = String.format("{\"error\": \"%s\", \"status\": 401}", message);
        response.getWriter().write(jsonResponse);
    }
}

