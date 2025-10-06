package com.skt.combustible.auth.infrastructure.repository;

import com.skt.combustible.auth.domain.entity.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Usuario usando MongoDB
 * 
 * @author Sistema SKT
 * @version 1.0
 */
@Repository
public interface UsuarioRepository extends MongoRepository<Usuario, String> {
    
    /**
     * Busca un usuario por username
     * 
     * @param username el username del usuario
     * @return Optional con el usuario encontrado
     */
    Optional<Usuario> findByUsername(String username);
    
    /**
     * Busca un usuario por email
     * 
     * @param email el email del usuario
     * @return Optional con el usuario encontrado
     */
    Optional<Usuario> findByEmail(String email);
    
    /**
     * Busca un usuario por username o email
     * 
     * @param username el username
     * @param email el email
     * @return Optional con el usuario encontrado
     */
    Optional<Usuario> findByUsernameOrEmail(String username, String email);
    
    /**
     * Verifica si existe un usuario con el username dado
     * 
     * @param username el username a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByUsername(String username);
    
    /**
     * Verifica si existe un usuario con el email dado
     * 
     * @param email el email a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByEmail(String email);
    
    /**
     * Busca usuarios activos por rol
     * 
     * @param rol el rol a buscar
     * @return lista de usuarios activos con ese rol
     */
    List<Usuario> findByRolAndActivoTrue(com.skt.combustible.shared.domain.enums.RolUsuario rol);
}
