package com.palomarg.repository;

import com.palomarg.model.entity.Origen;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz de repositorio para la entidad Origen.
 */
public interface OrigenRepository {
    List<Origen> findAll();
    Optional<Origen> findById(Long id);
    Origen save(Origen origen);
    boolean deleteById(Long id);
}
