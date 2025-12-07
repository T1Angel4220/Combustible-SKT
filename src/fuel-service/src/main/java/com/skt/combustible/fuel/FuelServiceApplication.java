package com.skt.combustible.fuel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Aplicación principal del servicio de combustible
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@SpringBootApplication(scanBasePackages = {
    "com.skt.combustible.fuel",
    "com.skt.combustible.shared"
})
@EnableMongoRepositories(basePackages = "com.skt.combustible.fuel.domain.repository")
public class FuelServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FuelServiceApplication.class, args);
    }
}
