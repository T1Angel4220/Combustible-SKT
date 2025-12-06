package com.skt.combustible.routes.infrastructure.security;

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
 * Filtro JWT para autenticación en Routes Service
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
            
            // Almacenar el token en ThreadLocal para que el interceptor gRPC pueda accederlo
            JwtTokenHolder.setToken(token);

            try {
                if (jwtService.validateToken(token)) {
                    String username = jwtService.getUsernameFromToken(token);
                    String rol = jwtService.getRolFromToken(token).name();

                    // Crear autenticación
                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                            new SimpleGrantedAuthority("ROLE_" + rol));

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            username, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    logger.debug("Usuario autenticado: {} con rol: {}", username, rol);
                } else {
                    logger.warn("Token JWT inválido en request a: {}", requestURI);
                    JwtTokenHolder.clear();
                    sendUnauthorizedResponse(response);
                    return;
                }
            } catch (Exception e) {
                logger.error("Error procesando token JWT: ", e);
                JwtTokenHolder.clear();
                sendUnauthorizedResponse(response);
                return;
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
            sendUnauthorizedResponse(response);
            return;
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Limpiar el token del ThreadLocal después de procesar la solicitud
            JwtTokenHolder.clear();
        }
    }

    /**
     * Verifica si el endpoint es público (no requiere autenticación)
     */
    private boolean isPublicEndpoint(String requestURI) {
        return requestURI.startsWith("/actuator/") ||
                requestURI.startsWith("/api/v1/routes/health") ||
                requestURI.startsWith("/api/v1/routes/info") ||
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
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = "{\"error\": \"Debes iniciar sesión para acceder a la gestión de rutas\", \"status\": 401}";
        response.getWriter().write(jsonResponse);
    }
}

