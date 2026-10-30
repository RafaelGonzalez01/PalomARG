package com.palomarg.repository;

import com.palomarg.model.entity.Campeonato;
import com.palomarg.model.entity.Carrera;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz de repositorio para la entidad Campeonato.
 */
public interface CampeonatoRepository {
    List<Campeonato> findAll();
    Optional<Campeonato> findById(Long id);
    Optional<Campeonato> findByExternalId(String externalId);
    Campeonato save(Campeonato campeonato);
    void vincularCarreras(Long campeonatoId, List<Carrera> carreras);
    List<Carrera> findCarrerasByCampeonatoId(Long campeonatoId);
    boolean deleteById(Long id);
    int countTotal();
}
