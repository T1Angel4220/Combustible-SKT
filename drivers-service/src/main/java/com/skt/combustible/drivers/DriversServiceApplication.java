package com.skt.combustible.drivers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Aplicación principal del servicio de choferes
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@SpringBootApplication(scanBasePackages = {
    "com.skt.combustible.drivers",
    "com.skt.combustible.shared"
})
public class DriversServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DriversServiceApplication.class, args);
    }
}
