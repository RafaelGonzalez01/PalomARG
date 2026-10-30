package com.palomarg.model.enums;

/**
 * Alcance territorial o institucional de un campeonato.
 */
public enum AlcanceCampeonato {
    LOCAL("Local"),
    INTERSOCIAL("Intersocial"),
    REGIONAL("Regional");

    private final String descripcion;

    AlcanceCampeonato(String descripcion) {
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
