package com.palomarg.model.enums;

/**
 * Categorías oficiales de un campeonato colombófilo.
 */
public enum CategoriaCampeonato {
    PICHONES("Pichones"),
    MIXTAS("Mixtas"),
    ADULTAS("Adultas"),
    FONDO("Fondo"),
    GRAN_FONDO("Gran Fondo"),
    SIN_CATEGORIA("Sin Categoría");

    private final String descripcion;

    CategoriaCampeonato(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
