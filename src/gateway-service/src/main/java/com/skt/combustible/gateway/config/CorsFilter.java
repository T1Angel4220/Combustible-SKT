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
        
        logger.debug("CORS Filter: {} {} from origin: {}", method, requestURI, origin);
        
        // Agregar headers CORS a todas las respuestas
        addCorsHeaders(response, origin);

        // Manejar preflight OPTIONS requests ANTES de pasar a otros filtros
        if ("OPTIONS".equalsIgnoreCase(method)) {
            logger.debug("CORS Filter: Handling OPTIONS preflight request");
            // Asegurar que todos los headers CORS estén presentes
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentLength(0);
            response.flushBuffer();
            logger.debug("CORS Filter: OPTIONS request handled successfully");
            return; // No continuar con la cadena de filtros para OPTIONS
        }

        // Para requests que no son OPTIONS, continuar con la cadena
        try {
            chain.doFilter(req, res);
        } finally {
            // Asegurar que los headers CORS estén presentes DESPUÉS del procesamiento
            // incluso si hubo una excepción
            addCorsHeaders(response, origin);
            logger.debug("CORS Filter: Headers added after request processing");
        }
    }
    
    private void addCorsHeaders(HttpServletResponse response, String origin) {
        if (origin != null && !origin.isEmpty()) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Credentials", "true");
        } else {
            // Si no hay origen, usar * pero sin credentials
            if (response.getHeader("Access-Control-Allow-Origin") == null) {
                response.setHeader("Access-Control-Allow-Origin", "*");
            }
        }
        
        // Siempre agregar estos headers
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH, HEAD");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Expose-Headers", "*");
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

