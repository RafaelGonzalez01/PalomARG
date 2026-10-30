package com.palomarg.model.entity;

import com.palomarg.model.enums.Colores;
import com.palomarg.model.enums.RolPaloma;
import com.palomarg.model.enums.SexoPaloma;
import com.palomarg.model.vo.Anillo;
import com.palomarg.model.vo.FechaNac;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad Paloma según las especificaciones del modelo:
 * - Propietario = Colombofilo (obligatorio).
 * - Palomar y Origen opcionales.
 * - Padre y Madre opcionales.
 * - Sexo, color y nacimiento opcionales (las palomas importadas no siempre los traen).
 * - Anillo estructurado en país, institución, año (2 dígitos) y número, formando clave única compuesta.
 * - Campos de sincronización e integración externa.
 */
public class Paloma {
    private Long id;
    private Anillo anillo;
    private Colombofilo propietario; // OBLIGATORIO
    private Palomar palomar;        // Opcional
    private Origen origen;          // Opcional
    private Paloma padre;           // Opcional
    private Paloma madre;           // Opcional
    private SexoPaloma sexo;        // Opcional (nullable)
    private Colores color;          // Opcional (nullable)
    private RolPaloma rol;          // Opcional (default VOLADORA)
    private FechaNac nacimiento;    // Opcional (nullable)

    // Sincronización e integración externa
    private String externalId;
    private String fuente;
    private LocalDateTime ultimaSincronizacion;

    public Paloma() {
        this.anillo = new Anillo();
        this.rol = RolPaloma.VOLADORA;
    }

    public Paloma(Anillo anillo, Colombofilo propietario) {
        this.anillo = anillo;
        this.propietario = propietario;
        this.rol = RolPaloma.VOLADORA;
    }

    public Paloma(String numeroAnillo, SexoPaloma sexo, Colores color, RolPaloma rol, FechaNac nacimiento) {
        setNumeroAnillo(numeroAnillo);
        this.sexo = sexo;
        this.color = color;
        this.rol = rol != null ? rol : RolPaloma.VOLADORA;
        this.nacimiento = nacimiento;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Anillo getAnillo() {
        return anillo;
    }

    public void setAnillo(Anillo anillo) {
        this.anillo = anillo;
    }

    /**
     * Retorna la clave del anillo formateada (ej: "ARG-FCA-24-100234" o "FCA-2021-100234").
     */
    public String getNumeroAnillo() {
        return anillo != null ? anillo.getCodigoCompleto() : "";
    }

    /**
     * Establece el anillo parseándolo a partir de texto compuesto.
     */
    public void setNumeroAnillo(String numeroAnillo) {
        if (numeroAnillo != null && !numeroAnillo.trim().isEmpty()) {
            this.anillo = Anillo.parse(numeroAnillo);
        }
    }

    public String getPais() {
        return anillo != null ? anillo.getPais() : "ARG";
    }

    public String getInstitucion() {
        return anillo != null ? anillo.getInstitucion() : "FCA";
    }

    public Integer getAnioAnillo() {
        return anillo != null ? anillo.getAnio() : null;
    }

    public String getNumeroSerieAnillo() {
        return anillo != null ? anillo.getNumero() : "";
    }

    public Colombofilo getPropietario() {
        return propietario;
    }

    public void setPropietario(Colombofilo propietario) {
        this.propietario = propietario;
    }

    public Palomar getPalomar() {
        return palomar;
    }

    public void setPalomar(Palomar palomar) {
        this.palomar = palomar;
    }

    public Origen getOrigen() {
        return origen;
    }

    public void setOrigen(Origen origen) {
        this.origen = origen;
    }

    public Paloma getPadre() {
        return padre;
    }

    public void setPadre(Paloma padre) {
        this.padre = padre;
    }

    public Paloma getMadre() {
        return madre;
    }

    public void setMadre(Paloma madre) {
        this.madre = madre;
    }

    public SexoPaloma getSexo() {
        return sexo;
    }

    public void setSexo(SexoPaloma sexo) {
        this.sexo = sexo;
    }

    public Colores getColor() {
        return color;
    }

    public void setColor(Colores color) {
        this.color = color;
    }

    public RolPaloma getRol() {
        return rol;
    }

    public void setRol(RolPaloma rol) {
        this.rol = rol;
    }

    public FechaNac getNacimiento() {
        return nacimiento;
    }

    public void setNacimiento(FechaNac nacimiento) {
        this.nacimiento = nacimiento;
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

    // Métodos auxiliares convenientes para UI y base de datos
    public String getPadreAnillo() {
        return padre != null ? padre.getNumeroAnillo() : null;
    }

    public String getMadreAnillo() {
        return madre != null ? madre.getNumeroAnillo() : null;
    }

    public String getOrigenNombre() {
        return origen != null ? origen.getNombre() : "Sin asignar";
    }

    public String getPropietarioNombre() {
        return propietario != null ? propietario.getNombre() : "Sin asignar";
    }

    @Override
    public String toString() {
        return getNumeroAnillo() + (propietario != null ? " (" + propietario.getNombre() + ")" : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Paloma paloma)) return false;
        return Objects.equals(getNumeroAnillo(), paloma.getNumeroAnillo());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNumeroAnillo());
    }
}
