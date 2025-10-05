package com.skt.combustible.auth.application.service;

import com.skt.combustible.auth.domain.entity.Usuario;
import com.skt.combustible.auth.infrastructure.repository.UsuarioRepository;
import com.skt.combustible.shared.domain.enums.RolUsuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * Servicio para inicializar datos por defecto en el sistema
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Service
public class DataInitializationService implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializationService.class);
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("Iniciando inicialización de datos por defecto...");
        
        initializeDefaultUsers();
        
        logger.info("Inicialización de datos completada");
    }
    
    /**
     * Inicializa usuarios por defecto en el sistema
     */
    private void initializeDefaultUsers() {
        // Usuario Administrador
        if (!usuarioRepository.existsByUsername("admin")) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setEmail("admin@skt.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNombre("Administrador");
            admin.setApellido("Sistema");
            admin.setRol(RolUsuario.ADMIN);
            admin.setPermisos(Arrays.asList(
                "USUARIOS_CREAR", "USUARIOS_LEER", "USUARIOS_ACTUALIZAR", "USUARIOS_ELIMINAR",
                "VEHICULOS_CREAR", "VEHICULOS_LEER", "VEHICULOS_ACTUALIZAR", "VEHICULOS_ELIMINAR",
                "RUTAS_CREAR", "RUTAS_LEER", "RUTAS_ACTUALIZAR", "RUTAS_ELIMINAR",
                "COMBUSTIBLE_CREAR", "COMBUSTIBLE_LEER", "COMBUSTIBLE_ACTUALIZAR", "COMBUSTIBLE_ELIMINAR",
                "REPORTES_GENERAR", "REPORTES_EXPORTAR"
            ));
            admin.setActivo(true);
            
            usuarioRepository.save(admin);
            logger.info("Usuario administrador creado: admin");
        }
        
        // Usuario Supervisor
        if (!usuarioRepository.existsByUsername("supervisor")) {
            Usuario supervisor = new Usuario();
            supervisor.setUsername("supervisor");
            supervisor.setEmail("supervisor@skt.com");
            supervisor.setPassword(passwordEncoder.encode("supervisor123"));
            supervisor.setNombre("Supervisor");
            supervisor.setApellido("Operaciones");
            supervisor.setRol(RolUsuario.SUPERVISOR);
            supervisor.setPermisos(Arrays.asList(
                "VEHICULOS_LEER", "VEHICULOS_ACTUALIZAR",
                "RUTAS_LEER", "RUTAS_ACTUALIZAR",
                "COMBUSTIBLE_LEER", "COMBUSTIBLE_ACTUALIZAR",
                "REPORTES_GENERAR", "REPORTES_EXPORTAR"
            ));
            supervisor.setActivo(true);
            
            usuarioRepository.save(supervisor);
            logger.info("Usuario supervisor creado: supervisor");
        }
        
        // Usuario Operador
        if (!usuarioRepository.existsByUsername("operador")) {
            Usuario operador = new Usuario();
            operador.setUsername("operador");
            operador.setEmail("operador@skt.com");
            operador.setPassword(passwordEncoder.encode("operador123"));
            operador.setNombre("Operador");
            operador.setApellido("Campo");
            operador.setRol(RolUsuario.OPERADOR);
            operador.setPermisos(Arrays.asList(
                "VEHICULOS_LEER",
                "RUTAS_LEER",
                "COMBUSTIBLE_CREAR", "COMBUSTIBLE_LEER"
            ));
            operador.setActivo(true);
            
            usuarioRepository.save(operador);
            logger.info("Usuario operador creado: operador");
        }
        
        // Usuario de prueba
        if (!usuarioRepository.existsByUsername("test")) {
            Usuario test = new Usuario();
            test.setUsername("test");
            test.setEmail("test@skt.com");
            test.setPassword(passwordEncoder.encode("test123"));
            test.setNombre("Usuario");
            test.setApellido("Prueba");
            test.setRol(RolUsuario.OPERADOR);
            test.setPermisos(Arrays.asList(
                "VEHICULOS_LEER",
                "RUTAS_LEER",
                "COMBUSTIBLE_LEER"
            ));
            test.setActivo(true);
            
            usuarioRepository.save(test);
            logger.info("Usuario de prueba creado: test");
        }
    }
}
