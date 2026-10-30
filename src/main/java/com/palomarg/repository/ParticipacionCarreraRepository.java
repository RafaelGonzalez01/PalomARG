package com.palomarg.repository;

import com.palomarg.model.entity.ParticipacionCarrera;

import java.util.List;

/**
 * Interfaz de repositorio para la entidad ParticipacionCarrera.
 */
public interface ParticipacionCarreraRepository {
    List<ParticipacionCarrera> findByCarreraId(Long carreraId);
    List<ParticipacionCarrera> findByPalomaAnillo(String anillo);
    List<ParticipacionCarrera> findByColombofiloId(Long colombofiloId);
    ParticipacionCarrera save(ParticipacionCarrera participacion);
    boolean deleteById(Long id);
    void recalcularClasificacion(Long carreraId);
}
