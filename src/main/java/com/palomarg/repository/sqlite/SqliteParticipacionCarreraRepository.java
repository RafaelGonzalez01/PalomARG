package com.palomarg.repository.sqlite;

import com.palomarg.database.DatabaseManager;
import com.palomarg.model.entity.ParticipacionCarrera;
import com.palomarg.model.enums.CategoriaEdad;
import com.palomarg.repository.CarreraRepository;
import com.palomarg.repository.ColombofiloRepository;
import com.palomarg.repository.PalomaRepository;
import com.palomarg.repository.ParticipacionCarreraRepository;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SqliteParticipacionCarreraRepository implements ParticipacionCarreraRepository {
    private final PalomaRepository palomaRepo;
    private final CarreraRepository carreraRepo;
    private final ColombofiloRepository colombofiloRepo;

    public SqliteParticipacionCarreraRepository() {
        this.palomaRepo = new SqlitePalomaRepository();
        this.carreraRepo = new SqliteCarreraRepository();
        this.colombofiloRepo = new SqliteColombofiloRepository();
    }

    public SqliteParticipacionCarreraRepository(PalomaRepository palomaRepo, CarreraRepository carreraRepo, ColombofiloRepository colombofiloRepo) {
        this.palomaRepo = palomaRepo;
        this.carreraRepo = carreraRepo;
        this.colombofiloRepo = colombofiloRepo;
    }

    @Override
    public List<ParticipacionCarrera> findByCarreraId(Long carreraId) {
        List<ParticipacionCarrera> lista = new ArrayList<>();
        if (carreraId == null) return lista;
        String sql = """
            SELECT id, carrera_id, paloma_id, paloma_anillo, id_colombofilo, puesto, puntos,
                   hora_llegada, segundos_vuelo, velocidad_metros_min, velocidad_kmh, distancia_km,
                   equipo, zonas, region, rango, hora_marcada, metodo_marcada, categoria_edad
            FROM participaciones
            WHERE carrera_id = ?
            ORDER BY CASE WHEN puesto IS NULL THEN 1 ELSE 0 END, puesto ASC, velocidad_metros_min DESC
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, carreraId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public List<ParticipacionCarrera> findByPalomaAnillo(String anillo) {
        List<ParticipacionCarrera> lista = new ArrayList<>();
        if (anillo == null || anillo.trim().isEmpty()) return lista;
        String sql = """
            SELECT id, carrera_id, paloma_id, paloma_anillo, id_colombofilo, puesto, puntos,
                   hora_llegada, segundos_vuelo, velocidad_metros_min, velocidad_kmh, distancia_km,
                   equipo, zonas, region, rango, hora_marcada, metodo_marcada, categoria_edad
            FROM participaciones
            WHERE paloma_anillo = ?
            ORDER BY hora_llegada DESC
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, anillo.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public List<ParticipacionCarrera> findByColombofiloId(Long colombofiloId) {
        List<ParticipacionCarrera> lista = new ArrayList<>();
        if (colombofiloId == null) return lista;
        String sql = """
            SELECT id, carrera_id, paloma_id, paloma_anillo, id_colombofilo, puesto, puntos,
                   hora_llegada, segundos_vuelo, velocidad_metros_min, velocidad_kmh, distancia_km,
                   equipo, zonas, region, rango, hora_marcada, metodo_marcada, categoria_edad
            FROM participaciones
            WHERE id_colombofilo = ?
            ORDER BY hora_llegada DESC
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, colombofiloId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public ParticipacionCarrera save(ParticipacionCarrera part) {
        part.calcularTiempoYVelocidades();

        if (part.getId() == null) {
            String sql = """
                INSERT INTO participaciones (carrera_id, paloma_id, paloma_anillo, id_colombofilo, puesto, puntos,
                                             hora_llegada, segundos_vuelo, velocidad_metros_min, velocidad_kmh, distancia_km,
                                             equipo, zonas, region, rango, hora_marcada, metodo_marcada, categoria_edad)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                setParameters(ps, part);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) part.setId(keys.getLong(1));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            String sql = """
                UPDATE participaciones SET carrera_id = ?, paloma_id = ?, paloma_anillo = ?, id_colombofilo = ?, puesto = ?, puntos = ?,
                                           hora_llegada = ?, segundos_vuelo = ?, velocidad_metros_min = ?, velocidad_kmh = ?, distancia_km = ?,
                                           equipo = ?, zonas = ?, region = ?, rango = ?, hora_marcada = ?, metodo_marcada = ?, categoria_edad = ?
                WHERE id = ?
            """;
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                setParameters(ps, part);
                ps.setLong(19, part.getId());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return part;
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM participaciones WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void recalcularClasificacion(Long carreraId) {
        List<ParticipacionCarrera> lista = findByCarreraId(carreraId);
        lista.forEach(ParticipacionCarrera::calcularTiempoYVelocidades);

        // Ordenar por velocidad en metros por minuto de mayor a menor
        lista.sort((p1, p2) -> {
            if (p1.getVelocidadMetrosMin() == null && p2.getVelocidadMetrosMin() == null) return 0;
            if (p1.getVelocidadMetrosMin() == null) return 1;
            if (p2.getVelocidadMetrosMin() == null) return -1;
            return Double.compare(p2.getVelocidadMetrosMin(), p1.getVelocidadMetrosMin());
        });

        int puesto = 1;
        double maxPuntos = 100.0;
        for (ParticipacionCarrera p : lista) {
            if (p.getVelocidadMetrosMin() != null && p.getVelocidadMetrosMin() > 0) {
                p.setPuesto(puesto);
                p.setPuntos(Math.max(10.0, maxPuntos - (puesto - 1) * 5.0));
                puesto++;
            } else {
                p.setPuesto(null);
                p.setPuntos(0.0);
            }
            save(p);
        }
    }

    private void setParameters(PreparedStatement ps, ParticipacionCarrera part) throws SQLException {
        ps.setLong(1, part.getCarrera() != null ? part.getCarrera().getId() : 0);
        ps.setObject(2, part.getPaloma() != null ? part.getPaloma().getId() : null);
        ps.setString(3, part.getPalomaAnillo());
        ps.setObject(4, part.getColombofilo() != null ? part.getColombofilo().getId() : null);
        ps.setObject(5, part.getPuesto());
        ps.setObject(6, part.getPuntos());
        ps.setString(7, part.getHoraLlegada() != null ? part.getHoraLlegada().toString() : null);
        ps.setObject(8, part.getTiempoVuelo() != null ? part.getTiempoVuelo().getSeconds() : null);
        ps.setObject(9, part.getVelocidadMetrosMin());
        ps.setObject(10, part.getVelocidadKmh());
        ps.setObject(11, part.getDistanciaKm());
        ps.setString(12, part.getEquipo());
        ps.setString(13, part.getZonasConcatenadas());
        ps.setString(14, part.getRegion());
        ps.setString(15, part.getRango());
        ps.setString(16, part.getHoraMarcada() != null ? part.getHoraMarcada().toString() : null);
        ps.setString(17, part.getMetodoMarcada());
        ps.setString(18, part.getCategoriaEdad() != null ? part.getCategoriaEdad().name() : null);
    }

    private ParticipacionCarrera mapResultSet(ResultSet rs) throws SQLException {
        ParticipacionCarrera part = new ParticipacionCarrera();
        part.setId(rs.getLong("id"));

        Number carreraId = (Number) rs.getObject("carrera_id");
        if (carreraId != null) carreraRepo.findById(carreraId.longValue()).ifPresent(part::setCarrera);

        String palomaAnillo = rs.getString("paloma_anillo");
        palomaRepo.findByNumeroAnillo(palomaAnillo).ifPresent(part::setPaloma);

        Number idCol = (Number) rs.getObject("id_colombofilo");
        if (idCol != null) colombofiloRepo.findById(idCol.longValue()).ifPresent(part::setColombofilo);

        part.setPuesto((Integer) rs.getObject("puesto"));

        Number pts = (Number) rs.getObject("puntos");
        if (pts != null) part.setPuntos(pts.doubleValue());

        String horaLlegadaStr = rs.getString("hora_llegada");
        if (horaLlegadaStr != null) {
            try { part.setHoraLlegada(LocalDateTime.parse(horaLlegadaStr)); } catch (Exception ignored) {}
        }

        Number segVuelo = (Number) rs.getObject("segundos_vuelo");
        if (segVuelo != null) {
            part.setTiempoVuelo(Duration.ofSeconds(segVuelo.longValue()));
        }

        Number velM = (Number) rs.getObject("velocidad_metros_min");
        if (velM != null) part.setVelocidadMetrosMin(velM.doubleValue());

        Number velK = (Number) rs.getObject("velocidad_kmh");
        if (velK != null) part.setVelocidadKmh(velK.doubleValue());

        Number dist = (Number) rs.getObject("distancia_km");
        if (dist != null) part.setDistanciaKm(dist.doubleValue());

        part.setEquipo(rs.getString("equipo"));

        String zonasStr = rs.getString("zonas");
        if (zonasStr != null && !zonasStr.trim().isEmpty()) {
            part.setZonas(new ArrayList<>(Arrays.asList(zonasStr.split("\\s*,\\s*"))));
        }

        part.setRegion(rs.getString("region"));
        part.setRango(rs.getString("rango"));

        String horaMarcadaStr = rs.getString("hora_marcada");
        if (horaMarcadaStr != null) {
            try { part.setHoraMarcada(LocalDateTime.parse(horaMarcadaStr)); } catch (Exception ignored) {}
        }

        part.setMetodoMarcada(rs.getString("metodo_marcada"));

        String catStr = rs.getString("categoria_edad");
        if (catStr != null) {
            try { part.setCategoriaEdad(CategoriaEdad.valueOf(catStr)); } catch (Exception ignored) {}
        }

        return part;
    }
}
