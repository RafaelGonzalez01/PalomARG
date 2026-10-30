package com.palomarg.repository.sqlite;

import com.palomarg.database.DatabaseManager;
import com.palomarg.model.entity.Zona;
import com.palomarg.repository.ZonaRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteZonaRepository implements ZonaRepository {

    @Override
    public Optional<Zona> findById(Long id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT id, nombre, descripcion FROM zonas WHERE id = ?";
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
    public Optional<Zona> findByNombre(String nombre) {
        if (nombre == null) return Optional.empty();
        String sql = "SELECT id, nombre, descripcion FROM zonas WHERE LOWER(nombre) = LOWER(?)";
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
    public List<Zona> findAll() {
        List<Zona> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, descripcion FROM zonas ORDER BY nombre";
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
    public Zona save(Zona zona) {
        if (zona.getId() == null) {
            String sql = "INSERT INTO zonas (nombre, descripcion) VALUES (?, ?)";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, zona.getNombre());
                ps.setString(2, zona.getDescripcion());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) zona.setId(keys.getLong(1));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            String sql = "UPDATE zonas SET nombre = ?, descripcion = ? WHERE id = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, zona.getNombre());
                ps.setString(2, zona.getDescripcion());
                ps.setLong(3, zona.getId());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return zona;
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM zonas WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Zona mapResultSet(ResultSet rs) throws SQLException {
        Zona z = new Zona();
        z.setId(rs.getLong("id"));
        z.setNombre(rs.getString("nombre"));
        z.setDescripcion(rs.getString("descripcion"));
        return z;
    }
}
