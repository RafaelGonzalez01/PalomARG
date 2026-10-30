package com.palomarg.model.entity;

import java.util.Objects;

/**
 * Entidad Sociedad / Asociación Colombófila.
 */
public class Sociedad {
    private Long id;
    private String nombre;
    private String localidad;
    private String codigo;

    public Sociedad() {}

    public Sociedad(String nombre, String localidad) {
        this.nombre = nombre;
        this.localidad = localidad;
    }

    public Sociedad(String nombre, String localidad, String codigo) {
        this.nombre = nombre;
        this.localidad = localidad;
        this.codigo = codigo;
    }

    public Sociedad(Long id, String nombre, String localidad) {
        this.id = id;
        this.nombre = nombre;
        this.localidad = localidad;
    }

    public Sociedad(Long id, String nombre, String localidad, String codigo) {
        this.id = id;
        this.nombre = nombre;
        this.localidad = localidad;
        this.codigo = codigo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getLocalidad() {
        return localidad;
    }

    public void setLocalidad(String localidad) {
        this.localidad = localidad;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    @Override
    public String toString() {
        return nombre + (localidad != null && !localidad.isEmpty() ? " (" + localidad + ")" : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sociedad sociedad)) return false;
        return Objects.equals(id, sociedad.id) || Objects.equals(nombre, sociedad.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nombre);
    }
}
