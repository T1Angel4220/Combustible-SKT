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
        
        // Agregar headers CORS a todas las respuestas
        if (origin != null && !origin.isEmpty()) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Credentials", "true");
        } else {
            response.setHeader("Access-Control-Allow-Origin", "*");
        }
        
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH, HEAD");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Expose-Headers", "*");

        // Continuar con la cadena de filtros
        chain.doFilter(req, res);
        
        // Asegurar que los headers CORS estén presentes después del procesamiento
        // (por si algún filtro anterior los eliminó)
        if (origin != null && !origin.isEmpty()) {
            response.setHeader("Access-Control-Allow-Origin", origin);
        } else if (response.getHeader("Access-Control-Allow-Origin") == null) {
            response.setHeader("Access-Control-Allow-Origin", "*");
        }
    }
}

