package com.skt.combustible.vehicles.infrastructure.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Aspecto para logging del servicio de vehículos
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Aspect
@Component
public class LoggingAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);
    
    /**
     * Logging antes de la ejecución de métodos
     */
    @Before("execution(* com.skt.combustible.vehicles.application.service.*.*(..))")
    public void logBefore(JoinPoint joinPoint) {
        logger.info("Ejecutando método: {} con argumentos: {}", 
                   joinPoint.getSignature().getName(), 
                   joinPoint.getArgs());
    }
    
    /**
     * Logging después de la ejecución de métodos
     */
    @After("execution(* com.skt.combustible.vehicles.application.service.*.*(..))")
    public void logAfter(JoinPoint joinPoint) {
        logger.info("Método completado: {}", joinPoint.getSignature().getName());
    }
    
    /**
     * Logging alrededor de la ejecución de métodos
     */
    @Around("execution(* com.skt.combustible.vehicles.application.service.*.*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            logger.info("Método {} ejecutado en {} ms", 
                       joinPoint.getSignature().getName(), 
                       executionTime);
            
            return result;
        } catch (Exception e) {
            logger.error("Error en método {}: {}", 
                        joinPoint.getSignature().getName(), 
                        e.getMessage());
            throw e;
        }
    }
}
