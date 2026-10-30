package com.palomarg.repository;

import com.palomarg.model.entity.Paloma;
import com.palomarg.model.enums.RolPaloma;
import com.palomarg.model.enums.SexoPaloma;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz de repositorio para la entidad Paloma.
 */
public interface PalomaRepository {
    List<Paloma> findAll();
    Optional<Paloma> findById(Long id);
    Optional<Paloma> findByNumeroAnillo(String anillo);
    Optional<Paloma> findByAnilloCompuesto(String pais, String institucion, Integer anio, String numero);
    Optional<Paloma> findByExternalId(String externalId);
    List<Paloma> findByPropietarioId(Long propietarioId);
    List<Paloma> search(String filtroAnillo, SexoPaloma filtroSexo, RolPaloma filtroRol);
    Paloma save(Paloma paloma);
    boolean deleteById(Long id);
    boolean deleteByNumeroAnillo(String anillo);
    int countTotal();
}
