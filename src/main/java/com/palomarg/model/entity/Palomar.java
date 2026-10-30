package com.palomarg.model.entity;

import java.util.Objects;

/**
 * Entidad Palomar según las especificaciones del modelo:
 * Dirección, latitud, longitud y pertenencia obligatoria a un Colombófilo.
 */
public class Palomar {
    private Long id;
    private String nombre;
    private String direccion;
    private Double latitud;
    private Double longitud;
    private Colombofilo colombofilo;

    // Campo de compatibilidad
    private String propietario;

    public Palomar() {}

    public Palomar(String nombre, String direccion, Colombofilo colombofilo) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.colombofilo = colombofilo;
    }

    public Palomar(Long id, String nombre, String direccion, Double latitud, Double longitud, Colombofilo colombofilo) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.latitud = latitud;
        this.longitud = longitud;
        this.colombofilo = colombofilo;
    }

    // Constructor de compatibilidad con versiones anteriores
    public Palomar(String nombre, String propietario, String ubicacion) {
        this.nombre = nombre;
        this.propietario = propietario;
        this.direccion = ubicacion;
    }

    public Palomar(Long id, String nombre, String propietario, String ubicacion) {
        this.id = id;
        this.nombre = nombre;
        this.propietario = propietario;
        this.direccion = ubicacion;
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

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public Colombofilo getColombofilo() {
        return colombofilo;
    }

    public void setColombofilo(Colombofilo colombofilo) {
        this.colombofilo = colombofilo;
        if (colombofilo != null) {
            this.propietario = colombofilo.getNombre();
        }
    }

    // Métodos de compatibilidad con la interfaz existente
    public String getPropietario() {
        if (colombofilo != null) {
            return colombofilo.getNombre();
        }
        return propietario != null ? propietario : "Sin asignar";
    }

    public void setPropietario(String propietario) {
        this.propietario = propietario;
    }

    public String getUbicacion() {
        return direccion != null ? direccion : "";
    }

    public void setUbicacion(String ubicacion) {
        this.direccion = ubicacion;
    }

    @Override
    public String toString() {
        return nombre + " (" + getPropietario() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Palomar palomar)) return false;
        return Objects.equals(id, palomar.id) || 
               (Objects.equals(nombre, palomar.nombre) && Objects.equals(getPropietario(), palomar.getPropietario()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nombre);
    }
}
