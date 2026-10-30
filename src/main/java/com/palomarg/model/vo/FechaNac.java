package com.palomarg.model.vo;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

/**
 * Representa la fecha o año de nacimiento de una paloma (FechaNac en el UML).
 * Permite manejar tanto fechas completas (día, mes, año) como solo el año de anillado,
 * que es muy común en la práctica colombófila.
 */
public class FechaNac {
    private Integer dia;
    private Integer mes;
    private Integer anio;

    public FechaNac() {
        this.anio = LocalDate.now().getYear();
    }

    public FechaNac(Integer anio) {
        this.anio = anio;
    }

    public FechaNac(Integer dia, Integer mes, Integer anio) {
        this.dia = dia;
        this.mes = mes;
        this.anio = anio;
    }

    public FechaNac(LocalDate fecha) {
        if (fecha != null) {
            this.dia = fecha.getDayOfMonth();
            this.mes = fecha.getMonthValue();
            this.anio = fecha.getYear();
        }
    }

    public static FechaNac fromLocalDate(LocalDate fecha) {
        return fecha != null ? new FechaNac(fecha) : null;
    }

    public static FechaNac parse(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }
        texto = texto.trim();
        // Formato dd/MM/yyyy o yyyy-MM-dd o solo yyyy
        if (texto.contains("/")) {
            String[] partes = texto.split("/");
            if (partes.length == 3) {
                return new FechaNac(Integer.parseInt(partes[0].trim()),
                                    Integer.parseInt(partes[1].trim()),
                                    Integer.parseInt(partes[2].trim()));
            }
        } else if (texto.contains("-")) {
            String[] partes = texto.split("-");
            if (partes.length == 3) {
                return new FechaNac(Integer.parseInt(partes[2].trim()),
                                    Integer.parseInt(partes[1].trim()),
                                    Integer.parseInt(partes[0].trim()));
            }
        } else if (texto.matches("\\d{4}")) {
            return new FechaNac(Integer.parseInt(texto));
        }
        return null;
    }

    public LocalDate toLocalDate() {
        if (anio != null && mes != null && dia != null) {
            try {
                return LocalDate.of(anio, mes, dia);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public int getEdad() {
        if (anio == null) return 0;
        LocalDate hoy = LocalDate.now();
        LocalDate nac = toLocalDate();
        if (nac != null) {
            return Period.between(nac, hoy).getYears();
        }
        return Math.max(0, hoy.getYear() - anio);
    }

    public Integer getDia() {
        return dia;
    }

    public void setDia(Integer dia) {
        this.dia = dia;
    }

    public Integer getMes() {
        return mes;
    }

    public void setMes(Integer mes) {
        this.mes = mes;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    @Override
    public String toString() {
        if (dia != null && mes != null && anio != null) {
            return String.format("%02d/%02d/%04d", dia, mes, anio);
        } else if (anio != null) {
            return String.valueOf(anio);
        }
        return "Sin fecha";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FechaNac fechaNac)) return false;
        return Objects.equals(dia, fechaNac.dia) &&
               Objects.equals(mes, fechaNac.mes) &&
               Objects.equals(anio, fechaNac.anio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dia, mes, anio);
    }
}
