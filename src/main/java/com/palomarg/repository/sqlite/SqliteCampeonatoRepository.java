package com.palomarg.repository.sqlite;

import com.palomarg.database.DatabaseManager;
import com.palomarg.model.entity.Campeonato;
import com.palomarg.model.entity.Carrera;
import com.palomarg.model.enums.AlcanceCampeonato;
import com.palomarg.model.enums.CategoriaCampeonato;
import com.palomarg.repository.CampeonatoRepository;
import com.palomarg.repository.CarreraRepository;
import com.palomarg.repository.SociedadRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteCampeonatoRepository implements CampeonatoRepository {
    private final SociedadRepository sociedadRepo;
    private final CarreraRepository carreraRepo;

    public SqliteCampeonatoRepository() {
        this.sociedadRepo = new SqliteSociedadRepository();
        this.carreraRepo = new SqliteCarreraRepository();
    }

    public SqliteCampeonatoRepository(SociedadRepository sociedadRepo, CarreraRepository carreraRepo) {
        this.sociedadRepo = sociedadRepo;
        this.carreraRepo = carreraRepo;
    }

    @Override
    public List<Campeonato> findAll() {
        List<Campeonato> lista = new ArrayList<>();
        String sql = """
            SELECT id, nombre, temporada, id_sociedad, alcance, clase, categoria,
                   external_id, fuente, ultima_sincronizacion
            FROM campeonatos
            ORDER BY temporada DESC, nombre
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Campeonato c = mapResultSet(rs);
                c.setCarreras(findCarrerasByCampeonatoId(c.getId()));
                lista.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public Optional<Campeonato> findById(Long id) {
        if (id == null) return Optional.empty();
        String sql = """
            SELECT id, nombre, temporada, id_sociedad, alcance, clase, categoria,
                   external_id, fuente, ultima_sincronizacion
            FROM campeonatos
            WHERE id = ?
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Campeonato c = mapResultSet(rs);
                    c.setCarreras(findCarrerasByCampeonatoId(c.getId()));
                    return Optional.of(c);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<Campeonato> findByExternalId(String externalId) {
        if (externalId == null) return Optional.empty();
        String sql = "SELECT id, nombre, temporada, id_sociedad, alcance, clase, categoria, external_id, fuente, ultima_sincronizacion FROM campeonatos WHERE external_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, externalId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Campeonato c = mapResultSet(rs);
                    c.setCarreras(findCarrerasByCampeonatoId(c.getId()));
                    return Optional.of(c);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Campeonato save(Campeonato c) {
        if (c.getId() == null) {
            String sql = """
                INSERT INTO campeonatos (nombre, temporada, id_sociedad, alcance, clase, categoria, external_id, fuente, ultima_sincronizacion)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                setParameters(ps, c);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) c.setId(keys.getLong(1));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            String sql = """
                UPDATE campeonatos SET nombre = ?, temporada = ?, id_sociedad = ?, alcance = ?, clase = ?, categoria = ?, external_id = ?, fuente = ?, ultima_sincronizacion = ?
                WHERE id = ?
            """;
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                setParameters(ps, c);
                ps.setLong(10, c.getId());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (c.getId() != null && c.getCarreras() != null) {
            vincularCarreras(c.getId(), c.getCarreras());
        }

        return c;
    }

    @Override
    public void vincularCarreras(Long campeonatoId, List<Carrera> carreras) {
        String deleteSql = "DELETE FROM campeonato_carreras WHERE id_campeonato = ?";
        String insertSql = "INSERT OR IGNORE INTO campeonato_carreras (id_campeonato, id_carrera) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement psDel = conn.prepareStatement(deleteSql)) {
                psDel.setLong(1, campeonatoId);
                psDel.executeUpdate();
            }
            try (PreparedStatement psIns = conn.prepareStatement(insertSql)) {
                for (Carrera car : carreras) {
                    if (car.getId() != null) {
                        psIns.setLong(1, campeonatoId);
                        psIns.setLong(2, car.getId());
                        psIns.addBatch();
                    }
                }
                psIns.executeBatch();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Carrera> findCarrerasByCampeonatoId(Long campeonatoId) {
        List<Carrera> lista = new ArrayList<>();
        String sql = "SELECT id_carrera FROM campeonato_carreras WHERE id_campeonato = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, campeonatoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    carreraRepo.findById(rs.getLong("id_carrera")).ifPresent(lista::add);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM campeonatos WHERE id = ?";
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
        String sql = "SELECT COUNT(*) FROM campeonatos";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void setParameters(PreparedStatement ps, Campeonato c) throws SQLException {
        ps.setString(1, c.getNombre());
        ps.setInt(2, c.getTemporada());
        ps.setObject(3, c.getSociedad() != null ? c.getSociedad().getId() : null);
        ps.setString(4, c.getAlcance() != null ? c.getAlcance().name() : AlcanceCampeonato.LOCAL.name());
        ps.setString(5, c.getClase() != null ? c.getClase() : "All");
        ps.setString(6, c.getCategoria() != null ? c.getCategoria().name() : CategoriaCampeonato.SIN_CATEGORIA.name());
        ps.setString(7, c.getExternalId());
        ps.setString(8, c.getFuente());
        ps.setString(9, c.getUltimaSincronizacion() != null ? c.getUltimaSincronizacion().toString() : null);
    }

    private Campeonato mapResultSet(ResultSet rs) throws SQLException {
        Campeonato c = new Campeonato();
        c.setId(rs.getLong("id"));
        c.setNombre(rs.getString("nombre"));
        c.setTemporada(rs.getInt("temporada"));

        Number idSoc = (Number) rs.getObject("id_sociedad");
        if (idSoc != null) {
            sociedadRepo.findById(idSoc.longValue()).ifPresent(c::setSociedad);
        }

        String alcStr = rs.getString("alcance");
        if (alcStr != null) {
            try { c.setAlcance(AlcanceCampeonato.valueOf(alcStr)); } catch (Exception ignored) {}
        }

        c.setClase(rs.getString("clase"));

        String catStr = rs.getString("categoria");
        if (catStr != null) {
            try { c.setCategoria(CategoriaCampeonato.valueOf(catStr)); } catch (Exception ignored) {}
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
