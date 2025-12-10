package com.skt.combustible.gateway.config;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Intercepta todas las respuestas ANTES de que se envíen al cliente
 * y agrega headers CORS explícitamente
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@ControllerAdvice
public class CorsResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private static final Logger logger = LoggerFactory.getLogger(CorsResponseBodyAdvice.class);

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true; // Interceptar todas las respuestas
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                   Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                   ServerHttpRequest request, ServerHttpResponse response) {
        
        // Obtener el HttpServletRequest original para acceder a los headers
        HttpServletRequest servletRequest = null;
        if (request instanceof ServletServerHttpRequest) {
            servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
        }

        String origin = null;
        if (servletRequest != null) {
            origin = servletRequest.getHeader("Origin");
        }

        HttpHeaders headers = response.getHeaders();
        String requestPath = request.getURI().getPath();
        
        logger.info("CORS ResponseBodyAdvice: Intercepting response for path: {}, origin: {}", requestPath, origin);
        
        // Agregar headers CORS explícitamente
        if (origin != null && !origin.isEmpty()) {
            headers.set("Access-Control-Allow-Origin", origin);
            headers.set("Access-Control-Allow-Credentials", "true");
            logger.info("CORS ResponseBodyAdvice: Added CORS headers with origin: {}", origin);
        } else {
            // Si no hay origen, usar * pero sin credentials
            headers.set("Access-Control-Allow-Origin", "*");
            logger.info("CORS ResponseBodyAdvice: Added CORS headers with wildcard origin");
        }
        
        // Siempre asegurar que estos headers estén presentes
        headers.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH, HEAD");
        headers.set("Access-Control-Max-Age", "3600");
        headers.set("Access-Control-Allow-Headers", "*");
        headers.set("Access-Control-Expose-Headers", "*");
        
        logger.info("CORS ResponseBodyAdvice: Final headers - Allow-Origin: {}", headers.getFirst("Access-Control-Allow-Origin"));

        return body;
    }
}

