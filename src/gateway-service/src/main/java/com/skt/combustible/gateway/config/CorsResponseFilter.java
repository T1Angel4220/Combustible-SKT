package com.skt.combustible.gateway.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filtro de respuesta CORS que asegura que los headers CORS
 * se agreguen a TODAS las respuestas, incluso en errores
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE - 1) // Ejecutar después del CorsFilter pero antes de otros filtros
public class CorsResponseFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // Obtener el origen de la petición
        String origin = request.getHeader("Origin");
        
        // Agregar headers CORS ANTES del procesamiento
        addCorsHeaders(response, origin);

        // Continuar con la cadena de filtros
        try {
            chain.doFilter(req, res);
        } finally {
            // Asegurar que los headers CORS estén presentes DESPUÉS del procesamiento
            // incluso si hubo una excepción
            addCorsHeaders(response, origin);
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
}

