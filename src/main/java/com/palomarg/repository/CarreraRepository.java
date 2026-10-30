package com.palomarg.repository;

import com.palomarg.model.entity.Carrera;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz de repositorio para la entidad Carrera.
 */
public interface CarreraRepository {
    List<Carrera> findAll();
    Optional<Carrera> findById(Long id);
    Optional<Carrera> findByIdWeb(String idWeb);
    Optional<Carrera> findByExternalId(String externalId);
    Carrera save(Carrera carrera);
    boolean deleteById(Long id);
    int countTotal();
}
