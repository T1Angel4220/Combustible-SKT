package com.skt.combustible.vehicles;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Aplicación principal del servicio de vehículos
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@SpringBootApplication(scanBasePackages = {
    "com.skt.combustible.vehicles",
    "com.skt.combustible.shared"
})
public class VehiclesServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(VehiclesServiceApplication.class, args);
    }
}
