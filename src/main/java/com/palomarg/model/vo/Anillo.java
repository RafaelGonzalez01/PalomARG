package com.palomarg.model.vo;

import java.io.Serializable;
import java.util.Objects;

/**
 * Representa el identificador único de anillo de una paloma,
 * compuesto por país, institución/federación, año (2 dígitos) y número de serie.
 */
public class Anillo implements Serializable, Comparable<Anillo> {
    private String pais;
    private String institucion;
    private Integer anio; // 2 dígitos (ej: 24 para 2024)
    private String numero;

    public Anillo() {
        this.pais = "ARG";
        this.institucion = "FCA";
    }

    public Anillo(String pais, String institucion, Integer anio, String numero) {
        this.pais = normalizarTexto(pais != null ? pais : "ARG");
        this.institucion = normalizarTexto(institucion != null ? institucion : "FCA");
        this.anio = normalizarAnio(anio);
        this.numero = normalizarTexto(numero);
    }

    public Anillo(String institucion, Integer anio, String numero) {
        this("ARG", institucion, anio, numero);
    }

    /**
     * Parsea un texto de anillo en formato como "ARG-FCA-24-100234", "FCA-24-100234" o "FCA-2024-100234".
     */
    public static Anillo parse(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }
        String[] partes = texto.trim().toUpperCase().split("[-/ ]+");
        if (partes.length == 4) {
            // ARG-FCA-24-100234
            String pais = partes[0];
            String inst = partes[1];
            int anio = parsearAnio(partes[2]);
            String num = partes[3];
            return new Anillo(pais, inst, anio, num);
        } else if (partes.length == 3) {
            // FCA-24-100234 o FCA-2024-100234
            String inst = partes[0];
            int anio = parsearAnio(partes[1]);
            String num = partes[2];
            return new Anillo("ARG", inst, anio, num);
        } else if (partes.length == 2) {
            // FCA-100234
            return new Anillo("ARG", partes[0], null, partes[1]);
        } else {
            // Cadena simple, se toma como número
            return new Anillo("ARG", "FCA", null, partes[0]);
        }
    }

    private static int parsearAnio(String str) {
        try {
            int val = Integer.parseInt(str);
            if (val >= 1900 && val < 2000) return val - 1900;
            if (val >= 2000) return val % 100;
            return val;
        } catch (Exception e) {
            return 0;
        }
    }

    private static Integer normalizarAnio(Integer anio) {
        if (anio == null) return null;
        if (anio >= 1900 && anio < 2000) return anio - 1900;
        if (anio >= 2000) return anio % 100;
        return anio;
    }

    private static String normalizarTexto(String str) {
        return str != null ? str.trim().toUpperCase() : null;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = normalizarTexto(pais);
    }

    public String getInstitucion() {
        return institucion;
    }

    public void setInstitucion(String institucion) {
        this.institucion = normalizarTexto(institucion);
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = normalizarAnio(anio);
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = normalizarTexto(numero);
    }

    /**
     * Retorna el año en formato de 4 dígitos si está disponible.
     */
    public Integer getAnioCompleto() {
        if (anio == null) return null;
        return anio >= 50 ? 1900 + anio : 2000 + anio;
    }

    /**
     * Representación formateada oficial del anillo (ej: "ARG-FCA-24-100234").
     */
    public String getCodigoCompleto() {
        StringBuilder sb = new StringBuilder();
        if (pais != null && !pais.isEmpty()) {
            sb.append(pais).append("-");
        }
        if (institucion != null && !institucion.isEmpty()) {
            sb.append(institucion).append("-");
        }
        if (anio != null) {
            sb.append(String.format("%02d", anio)).append("-");
        }
        sb.append(numero != null ? numero : "");
        return sb.toString();
    }

    @Override
    public String toString() {
        return getCodigoCompleto();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Anillo anillo1)) return false;
        return Objects.equals(pais, anillo1.pais) &&
               Objects.equals(institucion, anillo1.institucion) &&
               Objects.equals(anio, anillo1.anio) &&
               Objects.equals(numero, anillo1.numero);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pais, institucion, anio, numero);
    }

    @Override
    public int compareTo(Anillo o) {
        if (o == null) return 1;
        return this.getCodigoCompleto().compareTo(o.getCodigoCompleto());
    }
}
