package com.palomarg.model.enums;

/**
 * Estados posibles para un reclamo de palomar/colombófilo.
 */
public enum EstadoReclamo {
    PENDIENTE("Pendiente de Revisión"),
    APROBADO("Aprobado"),
    RECHAZADO("Rechazado");

    private final String descripcion;

    EstadoReclamo(String descripcion) {
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
