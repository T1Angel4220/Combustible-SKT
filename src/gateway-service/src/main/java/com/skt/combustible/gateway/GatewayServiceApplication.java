package com.skt.combustible.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Aplicación principal del API Gateway
 * 
 * @author Sistema SKT
 * @version 1.0.0
 */
@SpringBootApplication(scanBasePackages = {
    "com.skt.combustible.gateway",
    "com.skt.combustible.shared"
})
public class GatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }
}
