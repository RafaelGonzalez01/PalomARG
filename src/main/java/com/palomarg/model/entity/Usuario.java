package com.palomarg.model.entity;

import com.palomarg.model.enums.RolUsuario;

import java.util.Objects;

/**
 * Entidad Usuario para autenticación y autorización (USUARIO | MASTER).
 */
public class Usuario {
    private Long id;
    private String nombreUsuario;
    private String hashPassword;
    private RolUsuario rol;

    public Usuario() {
        this.rol = RolUsuario.USUARIO;
    }

    public Usuario(String nombreUsuario, String hashPassword, RolUsuario rol) {
        this.nombreUsuario = nombreUsuario;
        this.hashPassword = hashPassword;
        this.rol = rol;
    }

    public Usuario(Long id, String nombreUsuario, String hashPassword, RolUsuario rol) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.hashPassword = hashPassword;
        this.rol = rol;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario != null ? nombreUsuario.trim() : null;
    }

    public String getHashPassword() {
        return hashPassword;
    }

    public void setHashPassword(String hashPassword) {
        this.hashPassword = hashPassword;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public void setRol(RolUsuario rol) {
        this.rol = rol;
    }

    public boolean esMaster() {
        return this.rol == RolUsuario.MASTER;
    }

    @Override
    public String toString() {
        return nombreUsuario + " (" + (rol != null ? rol.name() : "") + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario usuario)) return false;
        return Objects.equals(id, usuario.id) || Objects.equals(nombreUsuario, usuario.nombreUsuario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nombreUsuario);
    }
}
