package com.palomarg.model.entity;

import java.util.Objects;

/**
 * Entidad AliasColombofilo para mapear variantes del nombre provenientes de la web
 * o planillas externas al registro unificado del Colombofilo.
 */
public class AliasColombofilo {
    private Long id;
    private String nombreWeb;
    private String nombreNormalizado;
    private String codigo;
    private String ciudad;
    private Colombofilo colombofilo;

    public AliasColombofilo() {}

    public AliasColombofilo(String nombreWeb, String ciudad, Colombofilo colombofilo) {
        setNombreWeb(nombreWeb);
        this.ciudad = ciudad;
        this.colombofilo = colombofilo;
    }

    public AliasColombofilo(Long id, String nombreWeb, String codigo, String ciudad, Colombofilo colombofilo) {
        this.id = id;
        setNombreWeb(nombreWeb);
        this.codigo = codigo;
        this.ciudad = ciudad;
        this.colombofilo = colombofilo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreWeb() {
        return nombreWeb;
    }

    public void setNombreWeb(String nombreWeb) {
        this.nombreWeb = nombreWeb;
        this.nombreNormalizado = Colombofilo.normalizarNombre(nombreWeb);
    }

    public String getNombreNormalizado() {
        if (nombreNormalizado == null && nombreWeb != null) {
            nombreNormalizado = Colombofilo.normalizarNombre(nombreWeb);
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
        this.codigo = codigo;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public Colombofilo getColombofilo() {
        return colombofilo;
    }

    public void setColombofilo(Colombofilo colombofilo) {
        this.colombofilo = colombofilo;
    }

    @Override
    public String toString() {
        return nombreWeb + " -> " + (colombofilo != null ? colombofilo.getNombre() : "Sin asignar");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AliasColombofilo that)) return false;
        return Objects.equals(id, that.id) || Objects.equals(getNombreNormalizado(), that.getNombreNormalizado());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, getNombreNormalizado());
    }
}
