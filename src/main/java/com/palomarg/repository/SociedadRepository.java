package com.palomarg.repository;

import com.palomarg.model.entity.Sociedad;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz de repositorio para la entidad Sociedad.
 */
public interface SociedadRepository {
    Optional<Sociedad> findById(Long id);
    Optional<Sociedad> findByNombre(String nombre);
    List<Sociedad> findAll();
    Sociedad save(Sociedad sociedad);
    boolean deleteById(Long id);
}
