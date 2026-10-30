package com.palomarg.model.enums;

/**
 * Sexo de la paloma según el diagrama UML.
 */
public enum SexoPaloma {
    MACHO("Macho", "♂"),
    HEMBRA("Hembra", "♀"),
    INDETERMINADO("Indeterminado", "?");

    private final String descripcion;
    private final String simbolo;

    SexoPaloma(String descripcion, String simbolo) {
        this.descripcion = descripcion;
        this.simbolo = simbolo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getSimbolo() {
        return simbolo;
    }

    @Override
    public String toString() {
        return descripcion + " " + simbolo;
    }
}
