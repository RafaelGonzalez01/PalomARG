package com.palomarg.repository;

import com.palomarg.model.entity.ReclamoPalomar;
import com.palomarg.model.enums.EstadoReclamo;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz de repositorio para la entidad ReclamoPalomar.
 */
public interface ReclamoPalomarRepository {
    Optional<ReclamoPalomar> findById(Long id);
    List<ReclamoPalomar> findByUsuarioId(Long usuarioId);
    List<ReclamoPalomar> findByEstado(EstadoReclamo estado);
    List<ReclamoPalomar> findAll();
    ReclamoPalomar save(ReclamoPalomar reclamo);
    boolean deleteById(Long id);
}
