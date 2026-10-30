package com.palomarg.model.enums;

/**
 * Categorías de carreras y campeonatos según el diagrama UML.
 */
public enum TipoCategoria {
    VELOCIDAD("Velocidad (< 300 km)"),
    MEDIO_FONDO("Medio Fondo (300 - 500 km)"),
    FONDO("Fondo (500 - 700 km)"),
    GRAN_FONDO("Gran Fondo (> 700 km)"),
    GENERAL("General / Mixta"),
    PICHONES("Pichones");

    private final String descripcion;

    TipoCategoria(String descripcion) {
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
