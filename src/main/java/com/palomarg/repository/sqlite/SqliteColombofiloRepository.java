package com.palomarg.repository.sqlite;

import com.palomarg.database.DatabaseManager;
import com.palomarg.model.entity.Colombofilo;
import com.palomarg.repository.ColombofiloRepository;
import com.palomarg.repository.SociedadRepository;
import com.palomarg.repository.UsuarioRepository;
import com.palomarg.repository.ZonaRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteColombofiloRepository implements ColombofiloRepository {
    private final SociedadRepository sociedadRepo;
    private final ZonaRepository zonaRepo;
    private final UsuarioRepository usuarioRepo;

    public SqliteColombofiloRepository() {
        this.sociedadRepo = new SqliteSociedadRepository();
        this.zonaRepo = new SqliteZonaRepository();
        this.usuarioRepo = new SqliteUsuarioRepository();
    }

    public SqliteColombofiloRepository(SociedadRepository sociedadRepo, ZonaRepository zonaRepo, UsuarioRepository usuarioRepo) {
        this.sociedadRepo = sociedadRepo;
        this.zonaRepo = zonaRepo;
        this.usuarioRepo = usuarioRepo;
    }

    @Override
    public Optional<Colombofilo> findById(Long id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT id, nombre, nombre_normalizado, codigo, localidad, id_sociedad, id_zona, id_usuario, external_id, fuente, ultima_sincronizacion FROM colombofilos WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<Colombofilo> findByNombreNormalizado(String nombreNormalizado) {
        if (nombreNormalizado == null) return Optional.empty();
        String sql = "SELECT id, nombre, nombre_normalizado, codigo, localidad, id_sociedad, id_zona, id_usuario, external_id, fuente, ultima_sincronizacion FROM colombofilos WHERE nombre_normalizado = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombreNormalizado.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<Colombofilo> findByExternalId(String externalId) {
        if (externalId == null) return Optional.empty();
        String sql = "SELECT id, nombre, nombre_normalizado, codigo, localidad, id_sociedad, id_zona, id_usuario, external_id, fuente, ultima_sincronizacion FROM colombofilos WHERE external_id = ?";
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
    public List<Colombofilo> findAll() {
        List<Colombofilo> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, nombre_normalizado, codigo, localidad, id_sociedad, id_zona, id_usuario, external_id, fuente, ultima_sincronizacion FROM colombofilos ORDER BY nombre";
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
    public List<Colombofilo> search(String query) {
        List<Colombofilo> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, nombre_normalizado, codigo, localidad, id_sociedad, id_zona, id_usuario, external_id, fuente, ultima_sincronizacion FROM colombofilos WHERE nombre_normalizado LIKE ? OR codigo LIKE ? OR localidad LIKE ? ORDER BY nombre";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String q = "%" + (query != null ? query.trim().toLowerCase() : "") + "%";
            ps.setString(1, q);
            ps.setString(2, q);
            ps.setString(3, q);
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
    public Colombofilo save(Colombofilo c) {
        if (c.getId() == null) {
            String sql = "INSERT INTO colombofilos (nombre, nombre_normalizado, codigo, localidad, id_sociedad, id_zona, id_usuario, external_id, fuente, ultima_sincronizacion) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, c.getNombre());
                ps.setString(2, c.getNombreNormalizado());
                ps.setString(3, c.getCodigo());
                ps.setString(4, c.getLocalidad());
                ps.setObject(5, c.getSociedad() != null ? c.getSociedad().getId() : null);
                ps.setObject(6, c.getZona() != null ? c.getZona().getId() : null);
                ps.setObject(7, c.getUsuario() != null ? c.getUsuario().getId() : null);
                ps.setString(8, c.getExternalId());
                ps.setString(9, c.getFuente());
                ps.setString(10, c.getUltimaSincronizacion() != null ? c.getUltimaSincronizacion().toString() : null);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) c.setId(keys.getLong(1));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            String sql = "UPDATE colombofilos SET nombre = ?, nombre_normalizado = ?, codigo = ?, localidad = ?, id_sociedad = ?, id_zona = ?, id_usuario = ?, external_id = ?, fuente = ?, ultima_sincronizacion = ? WHERE id = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, c.getNombre());
                ps.setString(2, c.getNombreNormalizado());
                ps.setString(3, c.getCodigo());
                ps.setString(4, c.getLocalidad());
                ps.setObject(5, c.getSociedad() != null ? c.getSociedad().getId() : null);
                ps.setObject(6, c.getZona() != null ? c.getZona().getId() : null);
                ps.setObject(7, c.getUsuario() != null ? c.getUsuario().getId() : null);
                ps.setString(8, c.getExternalId());
                ps.setString(9, c.getFuente());
                ps.setString(10, c.getUltimaSincronizacion() != null ? c.getUltimaSincronizacion().toString() : null);
                ps.setLong(11, c.getId());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return c;
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM colombofilos WHERE id = ?";
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
        String sql = "SELECT COUNT(*) FROM colombofilos";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Colombofilo mapResultSet(ResultSet rs) throws SQLException {
        Colombofilo c = new Colombofilo();
        c.setId(rs.getLong("id"));
        c.setNombre(rs.getString("nombre"));
        c.setNombreNormalizado(rs.getString("nombre_normalizado"));
        c.setCodigo(rs.getString("codigo"));
        c.setLocalidad(rs.getString("localidad"));

        Number idSoc = (Number) rs.getObject("id_sociedad");
        if (idSoc != null) {
            sociedadRepo.findById(idSoc.longValue()).ifPresent(c::setSociedad);
        }

        Number idZona = (Number) rs.getObject("id_zona");
        if (idZona != null) {
            zonaRepo.findById(idZona.longValue()).ifPresent(c::setZona);
        }

        Number idUsu = (Number) rs.getObject("id_usuario");
        if (idUsu != null) {
            usuarioRepo.findById(idUsu.longValue()).ifPresent(c::setUsuario);
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
