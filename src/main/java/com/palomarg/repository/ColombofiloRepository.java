package com.palomarg.repository;

import com.palomarg.model.entity.Colombofilo;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz de repositorio para la entidad Colombofilo.
 */
public interface ColombofiloRepository {
    Optional<Colombofilo> findById(Long id);
    Optional<Colombofilo> findByNombreNormalizado(String nombreNormalizado);
    Optional<Colombofilo> findByExternalId(String externalId);
    List<Colombofilo> findAll();
    List<Colombofilo> search(String query);
    Colombofilo save(Colombofilo colombofilo);
    boolean deleteById(Long id);
    int countTotal();
}
