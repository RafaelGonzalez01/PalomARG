package com.palomarg.model.enums;

/**
 * Categoría de edad de la paloma en la carrera.
 */
public enum CategoriaEdad {
    PICHON("Pichón (año en curso)"),
    ADULTA("Adulta (2 años o más)");

    private final String descripcion;
    
    CategoriaEdad(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
    
    public static CategoriaEdad calcular(int anioCarrera, int anioAnillo2Digitos) {
        int anioNacimiento = anioAnillo2Digitos < 100 ? 2000 + anioAnillo2Digitos : anioAnillo2Digitos;
        if (anioNacimiento >= anioCarrera) {
            return PICHON;
        } else {
            return ADULTA;
        }
    }
}
