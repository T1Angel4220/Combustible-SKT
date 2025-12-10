package com.skt.combustible.gateway.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filtro CORS global para permitir todas las solicitudes cross-origin
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(CorsFilter.class);

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // Obtener el origen de la petición
        String origin = request.getHeader("Origin");
        String method = request.getMethod();
        String requestURI = request.getRequestURI();
        
        // Si no hay Origin pero hay Referer, extraer el origen del Referer
        String referer = request.getHeader("Referer");
        if ((origin == null || origin.isEmpty()) && referer != null) {
            try {
                java.net.URL url = new java.net.URL(referer);
                origin = url.getProtocol() + "://" + url.getAuthority();
                logger.debug("CORS Filter: Extracted origin from Referer: {}", origin);
            } catch (Exception e) {
                logger.debug("CORS Filter: Could not extract origin from Referer: {}", referer);
            }
        }
        
        // En Render, los headers de proxy pueden afectar cómo se ve el origen
        // Loggear información adicional para debugging
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        String forwardedHost = request.getHeader("X-Forwarded-Host");
        
        logger.info("CORS Filter: {} {} from origin: {} | X-Forwarded-Proto: {} | X-Forwarded-Host: {} | Referer: {}", 
                method, requestURI, origin, forwardedProto, forwardedHost, referer);
        
        // Usar wrapper para asegurar que los headers se agreguen correctamente
        CorsResponseWrapper wrappedResponse = new CorsResponseWrapper(response, origin);
        
        // Agregar headers CORS ANTES del procesamiento
        addCorsHeaders(wrappedResponse, origin);

        // Manejar preflight OPTIONS requests ANTES de pasar a otros filtros
        if ("OPTIONS".equalsIgnoreCase(method)) {
            logger.info("CORS Filter: Handling OPTIONS preflight request from origin: {}", origin);
            // Asegurar que todos los headers CORS estén presentes
            wrappedResponse.setStatus(HttpServletResponse.SC_OK);
            wrappedResponse.setContentLength(0);
            wrappedResponse.flushBuffer();
            logger.info("CORS Filter: OPTIONS request handled successfully for origin: {}", origin);
            return; // No continuar con la cadena de filtros para OPTIONS
        }

        // Para requests que no son OPTIONS, continuar con la cadena usando el wrapper
        try {
            chain.doFilter(req, wrappedResponse);
        } finally {
            // Asegurar que los headers CORS estén presentes DESPUÉS del procesamiento
            // incluso si hubo una excepción
            addCorsHeaders(wrappedResponse, origin);
            logger.info("CORS Filter: Headers added after request processing. Origin: {}", origin);
        }
    }
    
    private void addCorsHeaders(HttpServletResponse response, String origin) {
        // Permitir cualquier origen válido
        if (origin != null && !origin.isEmpty()) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Credentials", "true");
            logger.debug("CORS Filter: Added Allow-Origin: {} with credentials", origin);
        } else {
            // Si no hay origen, usar * pero sin credentials
            if (response.getHeader("Access-Control-Allow-Origin") == null) {
                response.setHeader("Access-Control-Allow-Origin", "*");
                logger.debug("CORS Filter: Added Allow-Origin: * (no credentials)");
            }
        }
        
        // Siempre agregar estos headers
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH, HEAD");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Expose-Headers", "*");
        
        // Log final para debugging
        logger.debug("CORS Filter: Final Allow-Origin header: {}", response.getHeader("Access-Control-Allow-Origin"));
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No necesita inicialización
    }

    @Override
    public void destroy() {
        // No necesita limpieza
    }
}

