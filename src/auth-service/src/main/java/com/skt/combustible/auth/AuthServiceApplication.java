package com.skt.combustible.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal del servicio de autenticación SKT
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@SpringBootApplication(scanBasePackages = {
    "com.skt.combustible.auth",
    "com.skt.combustible.shared"
})
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
