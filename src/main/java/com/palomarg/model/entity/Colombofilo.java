package com.palomarg.model.entity;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad Colombófilo (criador / competidor).
 */
public class Colombofilo {
    private Long id;
    private String nombre;
    private String nombreNormalizado;
    private String codigo; // Opcional, ej: "[123]"
    private String localidad;
    private Sociedad sociedad;
    private Zona zona;
    private Usuario usuario; // Opcional

    // Campos de sincronización / integración externa
    private String externalId;
    private String fuente;
    private LocalDateTime ultimaSincronizacion;

    public Colombofilo() {}

    public Colombofilo(String nombre, String localidad) {
        setNombre(nombre);
        this.localidad = localidad;
    }

    public Colombofilo(Long id, String nombre, String codigo, String localidad, Sociedad sociedad, Zona zona) {
        this.id = id;
        setNombre(nombre);
        setCodigo(codigo);
        this.localidad = localidad;
        this.sociedad = sociedad;
        this.zona = zona;
    }

    public static String normalizarNombre(String texto) {
        if (texto == null) return "";
        String nfd = Normalizer.normalize(texto.trim().toLowerCase(), Normalizer.Form.NFD);
        return nfd.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                  .replaceAll("[^a-z0-9 ]", " ")
                  .replaceAll("\\s+", " ")
                  .trim();
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
        this.nombreNormalizado = normalizarNombre(nombre);
    }

    public String getNombreNormalizado() {
        if (nombreNormalizado == null && nombre != null) {
            nombreNormalizado = normalizarNombre(nombre);
        }
        return nombreNormalizado;
    }

    public void setNombreNormalizado(String nombreNormalizado) {
        this.nombreNormalizado = nombreNormalizado;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        if (codigo != null && !codigo.trim().isEmpty()) {
            String limpio = codigo.replace("[", "").replace("]", "").trim();
            this.codigo = "[" + limpio + "]";
        } else {
            this.codigo = null;
        }
    }

    public String getLocalidad() {
        return localidad;
    }

    public void setLocalidad(String localidad) {
        this.localidad = localidad;
    }

    public Sociedad getSociedad() {
        return sociedad;
    }

    public void setSociedad(Sociedad sociedad) {
        this.sociedad = sociedad;
    }

    public Zona getZona() {
        return zona;
    }

    public void setZona(Zona zona) {
        this.zona = zona;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getFuente() {
        return fuente;
    }

    public void setFuente(String fuente) {
        this.fuente = fuente;
    }

    public LocalDateTime getUltimaSincronizacion() {
        return ultimaSincronizacion;
    }

    public void setUltimaSincronizacion(LocalDateTime ultimaSincronizacion) {
        this.ultimaSincronizacion = ultimaSincronizacion;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (codigo != null && !codigo.isEmpty()) {
            sb.append(codigo).append(" ");
        }
        sb.append(nombre != null ? nombre : "Colombófilo");
        if (localidad != null && !localidad.isEmpty()) {
            sb.append(" (").append(localidad).append(")");
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Colombofilo that)) return false;
        return Objects.equals(id, that.id) || Objects.equals(getNombreNormalizado(), that.getNombreNormalizado());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, getNombreNormalizado());
    }
}
