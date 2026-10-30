package com.palomarg.model.enums;

/**
 * Rol o función de la paloma en el palomar según el diagrama UML.
 */
public enum RolPaloma {
    VOLADORA("Voladora (Competición)"),
    REPRODUCTORA("Reproductora (Cría)"),
    NODRIZA("Nodriza"),
    RETIRADA("Retirada");

    private final String descripcion;

    RolPaloma(String descripcion) {
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
