package com.skt.combustible.routes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Aplicación principal del servicio de rutas
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@SpringBootApplication(scanBasePackages = {
    "com.skt.combustible.routes",
    "com.skt.combustible.shared"
})
public class RoutesServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoutesServiceApplication.class, args);
    }
}
