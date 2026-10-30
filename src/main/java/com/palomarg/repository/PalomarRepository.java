package com.palomarg.repository;

import com.palomarg.model.entity.Palomar;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz de repositorio para la entidad Palomar.
 */
public interface PalomarRepository {
    List<Palomar> findAll();
    Optional<Palomar> findById(Long id);
    List<Palomar> findByColombofiloId(Long colombofiloId);
    Palomar save(Palomar palomar);
    boolean deleteById(Long id);
}
