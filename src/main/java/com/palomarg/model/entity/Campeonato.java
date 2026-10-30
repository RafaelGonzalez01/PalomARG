package com.palomarg.model.entity;

import com.palomarg.model.enums.AlcanceCampeonato;
import com.palomarg.model.enums.CategoriaCampeonato;
import com.palomarg.model.enums.TipoCategoria;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entidad Campeonato según las especificaciones del modelo:
 * Nombre, temporada (año), sociedad organizadora, alcance (LOCAL | INTERSOCIAL | REGIONAL),
 * clase ("All", "Class A", "Class B"), categoría oficial (PICHONES, MIXTAS, etc.),
 * relación MUCHOS A MUCHOS con Carrera y campos de sincronización externa.
 */
public class Campeonato {
    private Long id;
    private String nombre;
    private Integer temporada;
    private Sociedad sociedad;
    private AlcanceCampeonato alcance;
    private String clase; // "All", "Class A", "Class B"
    private CategoriaCampeonato categoria;
    private List<Carrera> carreras = new ArrayList<>();

    // Integración y sincronización externa
    private String externalId;
    private String fuente;
    private LocalDateTime ultimaSincronizacion;

    public Campeonato() {
        this.temporada = LocalDate.now().getYear();
        this.alcance = AlcanceCampeonato.LOCAL;
        this.clase = "All";
        this.categoria = CategoriaCampeonato.SIN_CATEGORIA;
    }

    public Campeonato(String nombre, Integer temporada, Sociedad sociedad, AlcanceCampeonato alcance, String clase, CategoriaCampeonato categoria) {
        this.nombre = nombre;
        this.temporada = temporada;
        this.sociedad = sociedad;
        this.alcance = alcance;
        this.clase = clase != null ? clase : "All";
        this.categoria = categoria != null ? categoria : CategoriaCampeonato.SIN_CATEGORIA;
    }

    public Campeonato(Long id, String nombre, Integer temporada, Sociedad sociedad, AlcanceCampeonato alcance, String clase, CategoriaCampeonato categoria) {
        this.id = id;
        this.nombre = nombre;
        this.temporada = temporada;
        this.sociedad = sociedad;
        this.alcance = alcance;
        this.clase = clase != null ? clase : "All";
        this.categoria = categoria != null ? categoria : CategoriaCampeonato.SIN_CATEGORIA;
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

    public Integer getTemporada() {
        return temporada;
    }

    public void setTemporada(Integer temporada) {
        this.temporada = temporada;
    }

    public Sociedad getSociedad() {
        return sociedad;
    }

    public void setSociedad(Sociedad sociedad) {
        this.sociedad = sociedad;
    }

    public AlcanceCampeonato getAlcance() {
        return alcance;
    }

    public void setAlcance(AlcanceCampeonato alcance) {
        this.alcance = alcance;
    }

    public String getClase() {
        return clase;
    }

    public void setClase(String clase) {
        this.clase = clase;
    }

    public CategoriaCampeonato getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaCampeonato categoria) {
        this.categoria = categoria;
    }

    public List<Carrera> getCarreras() {
        return carreras;
    }

    public void setCarreras(List<Carrera> carreras) {
        this.carreras = carreras != null ? carreras : new ArrayList<>();
    }

    public void agregarCarrera(Carrera carrera) {
        if (carrera != null && !this.carreras.contains(carrera)) {
            this.carreras.add(carrera);
        }
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

    // Compatibilidad con código previo que usaba TipoCategoria
    public TipoCategoria getTipoCategoria() {
        if (categoria == null) return TipoCategoria.GENERAL;
        return switch (categoria) {
            case PICHONES -> TipoCategoria.PICHONES;
            case FONDO -> TipoCategoria.FONDO;
            case GRAN_FONDO -> TipoCategoria.GRAN_FONDO;
            default -> TipoCategoria.GENERAL;
        };
    }

    public void setTipoCategoria(TipoCategoria tc) {
        if (tc != null) {
            this.categoria = switch (tc) {
                case PICHONES -> CategoriaCampeonato.PICHONES;
                case FONDO -> CategoriaCampeonato.FONDO;
                case GRAN_FONDO -> CategoriaCampeonato.GRAN_FONDO;
                default -> CategoriaCampeonato.MIXTAS;
            };
        }
    }

    @Override
    public String toString() {
        return nombre + " (" + temporada + " - " + (sociedad != null ? sociedad.getNombre() : "General") + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Campeonato that)) return false;
        return Objects.equals(id, that.id) || 
               (Objects.equals(nombre, that.nombre) && Objects.equals(temporada, that.temporada));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nombre, temporada);
    }
}
