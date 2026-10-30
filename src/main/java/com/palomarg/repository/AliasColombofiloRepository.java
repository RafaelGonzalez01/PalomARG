package com.palomarg.repository;

import com.palomarg.model.entity.AliasColombofilo;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz de repositorio para la entidad AliasColombofilo.
 */
public interface AliasColombofiloRepository {
    Optional<AliasColombofilo> findById(Long id);
    Optional<AliasColombofilo> findByNombreNormalizado(String nombreNormalizado);
    List<AliasColombofilo> findByColombofiloId(Long colombofiloId);
    List<AliasColombofilo> findAll();
    AliasColombofilo save(AliasColombofilo alias);
    boolean deleteById(Long id);
}
