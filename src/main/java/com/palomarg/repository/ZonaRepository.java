package com.palomarg.repository;

import com.palomarg.model.entity.Zona;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz de repositorio para la entidad Zona.
 */
public interface ZonaRepository {
    Optional<Zona> findById(Long id);
    Optional<Zona> findByNombre(String nombre);
    List<Zona> findAll();
    Zona save(Zona zona);
    boolean deleteById(Long id);
}
