package com.skt.combustible.vehicles.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import com.skt.combustible.vehicles.domain.repository.VehicleRepository;

/**
 * Configuración de scheduling para el servicio de vehículos
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    
    @Autowired
    private VehicleRepository vehicleRepository;
    
    /**
     * Scheduler de tareas personalizado
     */
    @Bean
    @Primary
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("vehicles-scheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(60);
        return scheduler;
    }
    
    /**
     * Tarea programada para limpieza de datos
     */
    @Scheduled(fixedRate = 3600000) // Cada hora
    public void cleanupTask() {
        // Implementar lógica de limpieza si es necesario
    }
    
    /**
     * Tarea programada para estadísticas
     */
    @Scheduled(fixedRate = 1800000) // Cada 30 minutos
    public void statisticsTask() {
        // Implementar lógica de estadísticas si es necesario
    }
}
