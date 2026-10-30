package com.palomarg.model.entity;

import com.palomarg.model.enums.TipoCategoria;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entidad Carrera:
 * Nombre, fecha y hora de suelta, ciudad, punto de suelta, coordenadas, clima, viento,
 * distancia de referencia (refKm), puntos a la primera, idWeb único, encestes manuales,
 * cantidad de marcadas y porcentaje de marcadas calculado.
 */
public class Carrera {
    private Long id;
    private String nombre;
    private LocalDateTime fechaHoraSuelta;
    private String ciudad;
    private String puntoSuelta;
    private Double latitudSuelta;
    private Double longitudSuelta;
    private String clima;
    private String viento;
    private Double refKm; // Distancia de referencia
    private Double puntosPrimera;
    private String idWeb; // Opcional, único
    private Integer encestes; // Opcional, número ingresado a mano
    private TipoCategoria tipoCategoria;
    private List<ParticipacionCarrera> participaciones = new ArrayList<>();

    // Integración y sincronización externa
    private String externalId;
    private String fuente;
    private LocalDateTime ultimaSincronizacion;

    public Carrera() {
        this.fechaHoraSuelta = LocalDateTime.now();
        this.tipoCategoria = TipoCategoria.VELOCIDAD;
    }

    public Carrera(String nombre, LocalDateTime fechaHoraSuelta, Double refKm, String clima, String viento, TipoCategoria tipoCategoria) {
        this.nombre = nombre;
        this.fechaHoraSuelta = fechaHoraSuelta;
        this.refKm = refKm;
        this.clima = clima;
        this.viento = viento;
        this.tipoCategoria = tipoCategoria;
    }

    public Carrera(Long id, String nombre, LocalDateTime fechaHoraSuelta, Double refKm, String clima, String viento, TipoCategoria tipoCategoria) {
        this.id = id;
        this.nombre = nombre;
        this.fechaHoraSuelta = fechaHoraSuelta;
        this.refKm = refKm;
        this.clima = clima;
        this.viento = viento;
        this.tipoCategoria = tipoCategoria;
    }

    /**
     * Cantidad de palomas marcadas/comprobadas en la carrera.
     */
    public int getMarcadas() {
        return participaciones != null ? participaciones.size() : 0;
    }

    /**
     * Porcentaje de marcadas respecto al total de encestes (solo si hay encestes registrados).
     */
    public Double getPorcentajeMarcadas() {
        if (encestes != null && encestes > 0) {
            double porc = ((double) getMarcadas() / encestes) * 100.0;
            return Math.round(porc * 100.0) / 100.0;
        }
        return null;
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

    public LocalDateTime getFechaHoraSuelta() {
        return fechaHoraSuelta;
    }

    public void setFechaHoraSuelta(LocalDateTime fechaHoraSuelta) {
        this.fechaHoraSuelta = fechaHoraSuelta;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getPuntoSuelta() {
        return puntoSuelta;
    }

    public void setPuntoSuelta(String puntoSuelta) {
        this.puntoSuelta = puntoSuelta;
    }

    public Double getLatitudSuelta() {
        return latitudSuelta;
    }

    public void setLatitudSuelta(Double latitudSuelta) {
        this.latitudSuelta = latitudSuelta;
    }

    public Double getLongitudSuelta() {
        return longitudSuelta;
    }

    public void setLongitudSuelta(Double longitudSuelta) {
        this.longitudSuelta = longitudSuelta;
    }

    public String getClima() {
        return clima;
    }

    public void setClima(String clima) {
        this.clima = clima;
    }

    public String getViento() {
        return viento;
    }

    public void setViento(String viento) {
        this.viento = viento;
    }

    public Double getRefKm() {
        return refKm;
    }

    public void setRefKm(Double refKm) {
        this.refKm = refKm;
    }

    // Compatibilidad con código anterior que llamaba getDistanciaKm()
    public Double getDistanciaKm() {
        return refKm;
    }

    public void setDistanciaKm(Double distanciaKm) {
        this.refKm = distanciaKm;
    }

    public Double getPuntosPrimera() {
        return puntosPrimera;
    }

    public void setPuntosPrimera(Double puntosPrimera) {
        this.puntosPrimera = puntosPrimera;
    }

    public String getIdWeb() {
        return idWeb;
    }

    public void setIdWeb(String idWeb) {
        this.idWeb = idWeb;
    }

    public Integer getEncestes() {
        return encestes;
    }

    public void setEncestes(Integer encestes) {
        this.encestes = encestes;
    }

    public TipoCategoria getTipoCategoria() {
        return tipoCategoria;
    }

    public void setTipoCategoria(TipoCategoria tipoCategoria) {
        this.tipoCategoria = tipoCategoria;
    }

    public List<ParticipacionCarrera> getParticipaciones() {
        return participaciones;
    }

    public void setParticipaciones(List<ParticipacionCarrera> participaciones) {
        this.participaciones = participaciones;
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

    public String getFechaHoraSueltaFormateada() {
        if (fechaHoraSuelta != null) {
            return fechaHoraSuelta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }
        return "Sin fecha";
    }

    @Override
    public String toString() {
        return nombre + (refKm != null ? " (" + refKm + " km)" : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Carrera carrera)) return false;
        return Objects.equals(id, carrera.id) || Objects.equals(nombre, carrera.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nombre);
    }
}
