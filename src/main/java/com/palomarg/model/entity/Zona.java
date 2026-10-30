package com.palomarg.model.entity;

import java.util.Objects;

/**
 * Entidad Zona geográfica colombófila (ej: Zona Norte, Zona Sur, etc.).
 */
public class Zona {
    private Long id;
    private String nombre;
    private String descripcion;

    public Zona() {}

    public Zona(String nombre) {
        this.nombre = nombre;
    }

    public Zona(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public Zona(Long id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return nombre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Zona zona)) return false;
        return Objects.equals(id, zona.id) || Objects.equals(nombre, zona.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nombre);
    }
}
