package com.skt.combustible.gateway.config;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Wrapper de respuesta HTTP que intercepta la escritura para asegurar
 * que los headers CORS estén presentes antes de enviar la respuesta
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
public class CorsResponseWrapper extends HttpServletResponseWrapper {

    private String origin;

    public CorsResponseWrapper(HttpServletResponse response, String origin) {
        super(response);
        this.origin = origin;
    }

    @Override
    public void flushBuffer() throws IOException {
        // Asegurar que los headers CORS estén presentes ANTES de flush
        ensureCorsHeaders();
        super.flushBuffer();
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        ensureCorsHeaders();
        return super.getWriter();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        ensureCorsHeaders();
        return super.getOutputStream();
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        ensureCorsHeaders();
        super.sendError(sc, msg);
    }

    @Override
    public void sendError(int sc) throws IOException {
        ensureCorsHeaders();
        super.sendError(sc);
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        ensureCorsHeaders();
        super.sendRedirect(location);
    }

    private void ensureCorsHeaders() {
        if (origin != null && !origin.isEmpty()) {
            setHeader("Access-Control-Allow-Origin", origin);
            setHeader("Access-Control-Allow-Credentials", "true");
        } else {
            if (getHeader("Access-Control-Allow-Origin") == null) {
                setHeader("Access-Control-Allow-Origin", "*");
            }
        }
        
        // Siempre asegurar que estos headers estén presentes
        setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH, HEAD");
        setHeader("Access-Control-Max-Age", "3600");
        setHeader("Access-Control-Allow-Headers", "*");
        setHeader("Access-Control-Expose-Headers", "*");
    }
}

