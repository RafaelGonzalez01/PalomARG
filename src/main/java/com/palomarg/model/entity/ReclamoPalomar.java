package com.palomarg.model.entity;

import com.palomarg.model.enums.EstadoReclamo;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad ReclamoPalomar para gestionar solicitudes de vinculación entre un usuario y un colombófilo/palomar.
 */
public class ReclamoPalomar {
    private Long id;
    private Usuario usuario;
    private Colombofilo colombofilo;
    private EstadoReclamo estado;
    private LocalDateTime fecha;
    private String comentario;

    public ReclamoPalomar() {
        this.estado = EstadoReclamo.PENDIENTE;
        this.fecha = LocalDateTime.now();
    }

    public ReclamoPalomar(Usuario usuario, Colombofilo colombofilo, String comentario) {
        this.usuario = usuario;
        this.colombofilo = colombofilo;
        this.estado = EstadoReclamo.PENDIENTE;
        this.fecha = LocalDateTime.now();
        this.comentario = comentario;
    }

    public ReclamoPalomar(Long id, Usuario usuario, Colombofilo colombofilo, EstadoReclamo estado, LocalDateTime fecha, String comentario) {
        this.id = id;
        this.usuario = usuario;
        this.colombofilo = colombofilo;
        this.estado = estado;
        this.fecha = fecha;
        this.comentario = comentario;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Colombofilo getColombofilo() {
        return colombofilo;
    }

    public void setColombofilo(Colombofilo colombofilo) {
        this.colombofilo = colombofilo;
    }

    public EstadoReclamo getEstado() {
        return estado;
    }

    public void setEstado(EstadoReclamo estado) {
        this.estado = estado;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    @Override
    public String toString() {
        return "Reclamo #" + id + " [" + estado + "] - " +
               (usuario != null ? usuario.getNombreUsuario() : "") + " -> " +
               (colombofilo != null ? colombofilo.getNombre() : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReclamoPalomar that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
