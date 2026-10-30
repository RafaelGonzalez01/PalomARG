package com.palomarg.model.enums;

/**
 * Colores y plumajes típicos de palomas mensajeras según el diagrama UML.
 */
public enum Colores {
    AZUL("Azul"),
    ZAINO("Zaino"),
    GOTEADO("Goteado"),
    RODILLO("Rodillo / Rodado"),
    ROJO("Rojo"),
    BLANCO("Blanco"),
    BAYO("Bayo"),
    GRIZEL("Grizel"),
    NEGRO("Negro"),
    MOSAICO("Mosaico"),
    OTRO("Otro");

    private final String nombre;

    Colores(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
