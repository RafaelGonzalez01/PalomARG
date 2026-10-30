package com.palomarg.model.entity;

import com.palomarg.model.enums.CategoriaEdad;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entidad ParticipacionCarrera según las especificaciones del modelo:
 * Registra exclusivamente las palomas marcadas/comprobadas en la carrera.
 * Incluye tiempo de vuelo (Duration, compatible con más de 24h), velocidades (m/m y km/h),
 * distancias, colombófilo de ese día, equipo, zonas, región, rango y método de marca.
 */
public class ParticipacionCarrera {
    private Long id;
    private Carrera carrera;
    private Paloma paloma;
    private Colombofilo colombofilo; // El competidor de ese día (no varía si la paloma se vende)
    private Integer puesto;
    private Double puntos;
    private LocalDateTime horaLlegada;
    private Duration tiempoVuelo; // Puede superar las 24 horas
    private Double velocidadMetrosMin;
    private Double velocidadKmh;
    private Double distanciaKm;
    private String equipo;
    private List<String> zonas = new ArrayList<>();
    private String region;
    private String rango;
    private LocalDateTime horaMarcada;
    private String metodoMarcada; // Ej: Manual, Electrónico Tauris, Benzing, etc.
    private CategoriaEdad categoriaEdad; // Opcional

    public ParticipacionCarrera() {}

    public ParticipacionCarrera(Paloma paloma, Carrera carrera, LocalDateTime horaLlegada) {
        this.paloma = paloma;
        this.carrera = carrera;
        this.horaLlegada = horaLlegada;
        this.horaMarcada = horaLlegada;
        if (paloma != null) {
            this.colombofilo = paloma.getPropietario();
        }
        if (carrera != null) {
            this.distanciaKm = carrera.getRefKm();
        }
        calcularTiempoYVelocidades();
    }

    /**
     * Calcula automáticamente el tiempo de vuelo y las velocidades en m/m y km/h.
     */
    public void calcularTiempoYVelocidades() {
        if (horaLlegada != null && carrera != null && carrera.getFechaHoraSuelta() != null) {
            this.tiempoVuelo = Duration.between(carrera.getFechaHoraSuelta(), horaLlegada);
        }

        Double dist = this.distanciaKm;
        if (dist == null && carrera != null) {
            dist = carrera.getRefKm();
            this.distanciaKm = dist;
        }

        if (tiempoVuelo != null && dist != null && dist > 0) {
            long segundos = tiempoVuelo.getSeconds();
            if (segundos > 0) {
                double minutos = segundos / 60.0;
                double horas = segundos / 3600.0;
                double metros = dist * 1000.0;
                this.velocidadMetrosMin = Math.round((metros / minutos) * 100.0) / 100.0;
                this.velocidadKmh = Math.round((dist / horas) * 100.0) / 100.0;
            }
        }
    }

    /**
     * Formatea la duración de vuelo de forma legible incluso si supera las 24 horas (ej: "28h 14m 32s").
     */
    public String getTiempoVueloFormateado() {
        if (tiempoVuelo == null) return "-";
        long totalSegundos = tiempoVuelo.getSeconds();
        long horas = totalSegundos / 3600;
        long minutos = (totalSegundos % 3600) / 60;
        long segundos = totalSegundos % 60;
        return String.format("%02dh %02dm %02ds", horas, minutos, segundos);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Carrera getCarrera() {
        return carrera;
    }

    public void setCarrera(Carrera carrera) {
        this.carrera = carrera;
    }

    public Paloma getPaloma() {
        return paloma;
    }

    public void setPaloma(Paloma paloma) {
        this.paloma = paloma;
    }

    public Colombofilo getColombofilo() {
        return colombofilo;
    }

    public void setColombofilo(Colombofilo colombofilo) {
        this.colombofilo = colombofilo;
    }

    public Integer getPuesto() {
        return puesto;
    }

    public void setPuesto(Integer puesto) {
        this.puesto = puesto;
    }

    public Double getPuntos() {
        return puntos;
    }

    public void setPuntos(Double puntos) {
        this.puntos = puntos;
    }

    public LocalDateTime getHoraLlegada() {
        return horaLlegada;
    }

    public void setHoraLlegada(LocalDateTime horaLlegada) {
        this.horaLlegada = horaLlegada;
        calcularTiempoYVelocidades();
    }

    public Duration getTiempoVuelo() {
        return tiempoVuelo;
    }

    public void setTiempoVuelo(Duration tiempoVuelo) {
        this.tiempoVuelo = tiempoVuelo;
    }

    public Double getVelocidadMetrosMin() {
        return velocidadMetrosMin;
    }

    public void setVelocidadMetrosMin(Double velocidadMetrosMin) {
        this.velocidadMetrosMin = velocidadMetrosMin;
    }

    public Double getVelocidadKmh() {
        return velocidadKmh;
    }

    public void setVelocidadKmh(Double velocidadKmh) {
        this.velocidadKmh = velocidadKmh;
    }

    // Compatibilidad con código previo
    public Double getVelocidadMpm() {
        return velocidadMetrosMin;
    }

    public void setVelocidadMpm(Double velocidadMpm) {
        this.velocidadMetrosMin = velocidadMpm;
    }

    public Double calcularVelocidadMpm() {
        calcularTiempoYVelocidades();
        return velocidadMetrosMin;
    }

    public Double getDistanciaKm() {
        return distanciaKm;
    }

    public void setDistanciaKm(Double distanciaKm) {
        this.distanciaKm = distanciaKm;
    }

    public String getEquipo() {
        return equipo;
    }

    public void setEquipo(String equipo) {
        this.equipo = equipo;
    }

    public List<String> getZonas() {
        return zonas;
    }

    public void setZonas(List<String> zonas) {
        this.zonas = zonas != null ? zonas : new ArrayList<>();
    }

    public String getZonasConcatenadas() {
        return zonas != null ? String.join(", ", zonas) : "";
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getRango() {
        return rango;
    }

    public void setRango(String rango) {
        this.rango = rango;
    }

    public LocalDateTime getHoraMarcada() {
        return horaMarcada;
    }

    public void setHoraMarcada(LocalDateTime horaMarcada) {
        this.horaMarcada = horaMarcada;
    }

    public String getMetodoMarcada() {
        return metodoMarcada;
    }

    public void setMetodoMarcada(String metodoMarcada) {
        this.metodoMarcada = metodoMarcada;
    }

    public CategoriaEdad getCategoriaEdad() {
        return categoriaEdad;
    }

    public void setCategoriaEdad(CategoriaEdad categoriaEdad) {
        this.categoriaEdad = categoriaEdad;
    }

    public String getHoraLlegadaFormateada() {
        if (horaLlegada != null) {
            return horaLlegada.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        }
        return "En vuelo / No registrada";
    }

    public String getPalomaAnillo() {
        return paloma != null ? paloma.getNumeroAnillo() : "";
    }

    public String getCarreraNombre() {
        return carrera != null ? carrera.getNombre() : "";
    }

    public String getColombofiloNombre() {
        return colombofilo != null ? colombofilo.getNombre() : "";
    }

    @Override
    public String toString() {
        return (paloma != null ? paloma.getNumeroAnillo() : "Paloma") + 
               " en " + (carrera != null ? carrera.getNombre() : "Carrera") + 
               (velocidadMetrosMin != null ? " - " + velocidadMetrosMin + " m/m" : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParticipacionCarrera that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
