package com.palomarg.repository.sqlite;

import com.palomarg.database.DatabaseManager;
import com.palomarg.model.entity.Sociedad;
import com.palomarg.repository.SociedadRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteSociedadRepository implements SociedadRepository {

    @Override
    public Optional<Sociedad> findById(Long id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT id, nombre, localidad, codigo FROM sociedades WHERE id = ?";
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
    public Optional<Sociedad> findByNombre(String nombre) {
        if (nombre == null) return Optional.empty();
        String sql = "SELECT id, nombre, localidad, codigo FROM sociedades WHERE LOWER(nombre) = LOWER(?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Sociedad> findAll() {
        List<Sociedad> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, localidad, codigo FROM sociedades ORDER BY nombre";
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
    public Sociedad save(Sociedad sociedad) {
        if (sociedad.getId() == null) {
            String sql = "INSERT INTO sociedades (nombre, localidad, codigo) VALUES (?, ?, ?)";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, sociedad.getNombre());
                ps.setString(2, sociedad.getLocalidad());
                ps.setString(3, sociedad.getCodigo());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) sociedad.setId(keys.getLong(1));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            String sql = "UPDATE sociedades SET nombre = ?, localidad = ?, codigo = ? WHERE id = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, sociedad.getNombre());
                ps.setString(2, sociedad.getLocalidad());
                ps.setString(3, sociedad.getCodigo());
                ps.setLong(4, sociedad.getId());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return sociedad;
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM sociedades WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Sociedad mapResultSet(ResultSet rs) throws SQLException {
        Sociedad s = new Sociedad();
        s.setId(rs.getLong("id"));
        s.setNombre(rs.getString("nombre"));
        s.setLocalidad(rs.getString("localidad"));
        s.setCodigo(rs.getString("codigo"));
        return s;
    }
}
