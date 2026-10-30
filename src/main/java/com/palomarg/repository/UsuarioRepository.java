package com.palomarg.repository;

import com.palomarg.model.entity.Usuario;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz de repositorio para la entidad Usuario.
 */
public interface UsuarioRepository {
    Optional<Usuario> findById(Long id);
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);
    List<Usuario> findAll();
    Usuario save(Usuario usuario);
    boolean deleteById(Long id);
}
