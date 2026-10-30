package com.palomarg.repository.sqlite;

import com.palomarg.database.DatabaseManager;
import com.palomarg.model.entity.Carrera;
import com.palomarg.model.enums.TipoCategoria;
import com.palomarg.repository.CarreraRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteCarreraRepository implements CarreraRepository {

    @Override
    public List<Carrera> findAll() {
        List<Carrera> lista = new ArrayList<>();
        String sql = """
            SELECT id, nombre, fecha_hora_suelta, ciudad, punto_suelta, latitud_suelta, longitud_suelta,
                   clima, viento, ref_km, puntos_primera, id_web, encestes, tipo_categoria,
                   external_id, fuente, ultima_sincronizacion
            FROM carreras
            ORDER BY fecha_hora_suelta DESC
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public Optional<Carrera> findById(Long id) {
        if (id == null) return Optional.empty();
        String sql = """
            SELECT id, nombre, fecha_hora_suelta, ciudad, punto_suelta, latitud_suelta, longitud_suelta,
                   clima, viento, ref_km, puntos_primera, id_web, encestes, tipo_categoria,
                   external_id, fuente, ultima_sincronizacion
            FROM carreras
            WHERE id = ?
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<Carrera> findByIdWeb(String idWeb) {
        if (idWeb == null) return Optional.empty();
        String sql = "SELECT id, nombre, fecha_hora_suelta, ciudad, punto_suelta, latitud_suelta, longitud_suelta, clima, viento, ref_km, puntos_primera, id_web, encestes, tipo_categoria, external_id, fuente, ultima_sincronizacion FROM carreras WHERE id_web = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idWeb);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<Carrera> findByExternalId(String externalId) {
        if (externalId == null) return Optional.empty();
        String sql = "SELECT id, nombre, fecha_hora_suelta, ciudad, punto_suelta, latitud_suelta, longitud_suelta, clima, viento, ref_km, puntos_primera, id_web, encestes, tipo_categoria, external_id, fuente, ultima_sincronizacion FROM carreras WHERE external_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, externalId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Carrera save(Carrera carrera) {
        if (carrera.getId() == null) {
            String sql = """
                INSERT INTO carreras (nombre, fecha_hora_suelta, ciudad, punto_suelta, latitud_suelta, longitud_suelta,
                                      clima, viento, ref_km, puntos_primera, id_web, encestes, tipo_categoria,
                                      external_id, fuente, ultima_sincronizacion)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                setParameters(ps, carrera);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        carrera.setId(keys.getLong(1));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            String sql = """
                UPDATE carreras SET nombre = ?, fecha_hora_suelta = ?, ciudad = ?, punto_suelta = ?, latitud_suelta = ?, longitud_suelta = ?,
                                   clima = ?, viento = ?, ref_km = ?, puntos_primera = ?, id_web = ?, encestes = ?, tipo_categoria = ?,
                                   external_id = ?, fuente = ?, ultima_sincronizacion = ?
                WHERE id = ?
            """;
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                setParameters(ps, carrera);
                ps.setLong(17, carrera.getId());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return carrera;
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM carreras WHERE id = ?";
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
    public int countTotal() {
        String sql = "SELECT COUNT(*) FROM carreras";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void setParameters(PreparedStatement ps, Carrera c) throws SQLException {
        ps.setString(1, c.getNombre());
        ps.setString(2, c.getFechaHoraSuelta() != null ? c.getFechaHoraSuelta().toString() : null);
        ps.setString(3, c.getCiudad());
        ps.setString(4, c.getPuntoSuelta());
        ps.setObject(5, c.getLatitudSuelta());
        ps.setObject(6, c.getLongitudSuelta());
        ps.setString(7, c.getClima());
        ps.setString(8, c.getViento());
        ps.setObject(9, c.getRefKm());
        ps.setObject(10, c.getPuntosPrimera());
        ps.setString(11, c.getIdWeb());
        ps.setObject(12, c.getEncestes());
        ps.setString(13, c.getTipoCategoria() != null ? c.getTipoCategoria().name() : null);
        ps.setString(14, c.getExternalId());
        ps.setString(15, c.getFuente());
        ps.setString(16, c.getUltimaSincronizacion() != null ? c.getUltimaSincronizacion().toString() : null);
    }

    private Carrera mapResultSet(ResultSet rs) throws SQLException {
        Carrera c = new Carrera();
        c.setId(rs.getLong("id"));
        c.setNombre(rs.getString("nombre"));

        String fechaStr = rs.getString("fecha_hora_suelta");
        if (fechaStr != null) {
            try { c.setFechaHoraSuelta(LocalDateTime.parse(fechaStr)); } catch (Exception ignored) {}
        }

        c.setCiudad(rs.getString("ciudad"));
        c.setPuntoSuelta(rs.getString("punto_suelta"));

        Number lat = (Number) rs.getObject("latitud_suelta");
        if (lat != null) c.setLatitudSuelta(lat.doubleValue());

        Number lon = (Number) rs.getObject("longitud_suelta");
        if (lon != null) c.setLongitudSuelta(lon.doubleValue());

        c.setClima(rs.getString("clima"));
        c.setViento(rs.getString("viento"));

        Number refKm = (Number) rs.getObject("ref_km");
        if (refKm != null) c.setRefKm(refKm.doubleValue());

        Number ptsPri = (Number) rs.getObject("puntos_primera");
        if (ptsPri != null) c.setPuntosPrimera(ptsPri.doubleValue());

        c.setIdWeb(rs.getString("id_web"));
        c.setEncestes((Integer) rs.getObject("encestes"));

        String catStr = rs.getString("tipo_categoria");
        if (catStr != null) {
            try { c.setTipoCategoria(TipoCategoria.valueOf(catStr)); } catch (Exception ignored) {}
        }

        c.setExternalId(rs.getString("external_id"));
        c.setFuente(rs.getString("fuente"));

        String ultSinc = rs.getString("ultima_sincronizacion");
        if (ultSinc != null) {
            try { c.setUltimaSincronizacion(LocalDateTime.parse(ultSinc)); } catch (Exception ignored) {}
        }

        return c;
    }
}
