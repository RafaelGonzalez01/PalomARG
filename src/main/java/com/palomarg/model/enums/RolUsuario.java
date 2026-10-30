package com.palomarg.model.enums;

/**
 * Roles de usuario en el sistema.
 */
public enum RolUsuario {
    USUARIO("Usuario Estándar"),
    MASTER("Administrador Master");

    private final String descripcion;

    RolUsuario(String descripcion) {
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
