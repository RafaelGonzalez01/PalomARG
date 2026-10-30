package com.palomarg.service;

import com.palomarg.model.entity.Colombofilo;
import com.palomarg.repository.ColombofiloRepository;
import com.palomarg.repository.PalomaRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de búsqueda difusa de colombófilos.
 * Combina similitud de Jaccard por tokens (peso 0.6) con Jaro-Winkler sobre el nombre completo (peso 0.4).
 * Aplica refuerzos por código y ciudad coincidentes.
 * Solo devuelve colombófilos sin usuario vinculado.
 */
public class BusquedaColombofiloService {

    /** Conectores/palabras vacías que se ignoran al tokenizar. */
    private static final Set<String> CONECTORES = Set.of(
        "y", "e", "hijos", "familia", "hnos", "de", "del", "los", "las"
    );

    private final ColombofiloRepository colombofiloRepo;
    private final PalomaRepository palomaRepo;

    public BusquedaColombofiloService(ColombofiloRepository colombofiloRepo, PalomaRepository palomaRepo) {
        this.colombofiloRepo = colombofiloRepo;
        this.palomaRepo = palomaRepo;
    }

    /**
     * Representa un candidato de vinculación con su score de similitud.
     */
    public record CandidatoPalomar(Colombofilo colombofilo, double score, int cantidadPalomas) {
        public String formatoListado() {
            String codigo = colombofilo.getCodigo() != null ? colombofilo.getCodigo() + " " : "";
            String localidad = colombofilo.getLocalidad() != null ? " - " + colombofilo.getLocalidad() : "";
            return codigo + colombofilo.getNombre() + localidad + " · " + cantidadPalomas + " paloma" + (cantidadPalomas != 1 ? "s" : "");
        }
    }

    /**
     * Busca colombófilos candidatos para vincular (sin usuario asignado).
     *
     * @param query texto de búsqueda
     * @return lista de hasta 5 candidatos ordenados por score descendente
     */
    public List<CandidatoPalomar> buscarCandidatos(String query) {
        if (query == null || query.isBlank() || query.trim().length() < 2) {
            return Collections.emptyList();
        }

        String queryNorm = Colombofilo.normalizarNombre(query);
        Set<String> tokensQuery = tokenizar(queryNorm);

        // Extraer posible código de la consulta (ej: "[101]" o "101")
        String codigoQuery = extraerCodigo(query);

        // Extraer posible ciudad de la consulta (última palabra significativa)
        String ciudadQuery = extraerCiudad(queryNorm);

        // Obtener todos los colombófilos sin usuario
        List<Colombofilo> sinUsuario = colombofiloRepo.findAll().stream()
            .filter(c -> c.getUsuario() == null)
            .collect(Collectors.toList());

        List<CandidatoPalomar> candidatos = new ArrayList<>();

        for (Colombofilo c : sinUsuario) {
            String nombreNorm = c.getNombreNormalizado() != null
                ? c.getNombreNormalizado()
                : Colombofilo.normalizarNombre(c.getNombre());

            Set<String> tokensColombofilo = tokenizar(nombreNorm);

            double jaccard = similitudJaccard(tokensQuery, tokensColombofilo);
            double jaroWinkler = jaroWinkler(queryNorm, nombreNorm);

            double score = 0.6 * jaccard + 0.4 * jaroWinkler;

            // Refuerzo por código coincidente
            if (codigoQuery != null && c.getCodigo() != null) {
                String codigoCandidato = c.getCodigo().replace("[", "").replace("]", "").trim();
                if (codigoQuery.equalsIgnoreCase(codigoCandidato)) {
                    score += 0.2;
                }
            }

            // Refuerzo por ciudad/localidad coincidente
            if (ciudadQuery != null && c.getLocalidad() != null) {
                String locNorm = Colombofilo.normalizarNombre(c.getLocalidad());
                if (locNorm.contains(ciudadQuery) || ciudadQuery.contains(locNorm)) {
                    score += 0.1;
                }
            }

            if (score > 0.05) {
                int cantPalomas = contarPalomas(c);
                candidatos.add(new CandidatoPalomar(c, Math.min(score, 1.0), cantPalomas));
            }
        }

        // Ordenar por score descendente y tomar los 5 mejores
        candidatos.sort(Comparator.comparingDouble(CandidatoPalomar::score).reversed());
        return candidatos.subList(0, Math.min(5, candidatos.size()));
    }

    /**
     * Cuenta las palomas de un colombófilo.
     */
    private int contarPalomas(Colombofilo c) {
        if (c.getId() == null || palomaRepo == null) return 0;
        try {
            return palomaRepo.findByPropietarioId(c.getId()).size();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Tokeniza un texto normalizado ignorando conectores.
     */
    private Set<String> tokenizar(String texto) {
        if (texto == null || texto.isBlank()) return Collections.emptySet();
        Set<String> tokens = new HashSet<>();
        for (String token : texto.split("\\s+")) {
            if (!token.isBlank() && !CONECTORES.contains(token)) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    /**
     * Similitud de Jaccard entre dos conjuntos de tokens.
     */
    private double similitudJaccard(Set<String> a, Set<String> b) {
        if (a.isEmpty() && b.isEmpty()) return 1.0;
        if (a.isEmpty() || b.isEmpty()) return 0.0;

        Set<String> interseccion = new HashSet<>(a);
        interseccion.retainAll(b);

        Set<String> union = new HashSet<>(a);
        union.addAll(b);

        return (double) interseccion.size() / union.size();
    }

    /**
     * Calcula la similitud Jaro-Winkler entre dos cadenas.
     * Implementación pura Java, sin librerías externas.
     */
    public static double jaroWinkler(String s1, String s2) {
        if (s1 == null) s1 = "";
        if (s2 == null) s2 = "";
        if (s1.equals(s2)) return 1.0;
        if (s1.isEmpty() || s2.isEmpty()) return 0.0;

        double jaro = jaro(s1, s2);

        // Calcular prefijo común (máx 4 caracteres)
        int prefijo = 0;
        int maxPrefijo = Math.min(4, Math.min(s1.length(), s2.length()));
        for (int i = 0; i < maxPrefijo; i++) {
            if (s1.charAt(i) == s2.charAt(i)) prefijo++;
            else break;
        }

        // Jaro-Winkler: p = 0.1 (factor de escala estándar)
        return jaro + prefijo * 0.1 * (1.0 - jaro);
    }

    /**
     * Calcula la similitud Jaro entre dos cadenas.
     */
    private static double jaro(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();

        int ventana = Math.max(len1, len2) / 2 - 1;
        if (ventana < 0) ventana = 0;

        boolean[] coincideS1 = new boolean[len1];
        boolean[] coincideS2 = new boolean[len2];
        int matches = 0;

        for (int i = 0; i < len1; i++) {
            int inicio = Math.max(0, i - ventana);
            int fin = Math.min(i + ventana + 1, len2);
            for (int j = inicio; j < fin; j++) {
                if (!coincideS2[j] && s1.charAt(i) == s2.charAt(j)) {
                    coincideS1[i] = true;
                    coincideS2[j] = true;
                    matches++;
                    break;
                }
            }
        }

        if (matches == 0) return 0.0;

        // Contar transposiciones
        int transposiciones = 0;
        int k = 0;
        for (int i = 0; i < len1; i++) {
            if (coincideS1[i]) {
                while (!coincideS2[k]) k++;
                if (s1.charAt(i) != s2.charAt(k)) transposiciones++;
                k++;
            }
        }

        return (
            (double) matches / len1 +
            (double) matches / len2 +
            (double) (matches - transposiciones / 2.0) / matches
        ) / 3.0;
    }

    /**
     * Extrae un código numérico de la consulta (ej: "[101]" → "101").
     */
    private String extraerCodigo(String query) {
        if (query == null) return null;
        // Buscar patrón [nnn] o solo número
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\[?(\\d+)]?").matcher(query);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    /**
     * Extrae la posible ciudad del texto normalizado (última palabra significativa).
     */
    private String extraerCiudad(String queryNorm) {
        if (queryNorm == null || queryNorm.isBlank()) return null;
        String[] palabras = queryNorm.split("\\s+");
        for (int i = palabras.length - 1; i >= 0; i--) {
            if (!CONECTORES.contains(palabras[i]) && palabras[i].length() > 3) {
                return palabras[i];
            }
        }
        return null;
    }
}
