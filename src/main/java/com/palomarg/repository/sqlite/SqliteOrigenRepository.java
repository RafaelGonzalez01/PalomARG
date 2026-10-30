package com.palomarg.repository.sqlite;

import com.palomarg.database.DatabaseManager;
import com.palomarg.model.entity.Origen;
import com.palomarg.repository.OrigenRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteOrigenRepository implements OrigenRepository {

    @Override
    public List<Origen> findAll() {
        List<Origen> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, descripcion FROM origenes ORDER BY nombre";
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
    public Optional<Origen> findById(Long id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT id, nombre, descripcion FROM origenes WHERE id = ?";
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
    public Origen save(Origen origen) {
        if (origen.getId() == null) {
            String sql = "INSERT INTO origenes (nombre, descripcion) VALUES (?, ?)";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, origen.getNombre());
                ps.setString(2, origen.getDescripcion());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        origen.setId(keys.getLong(1));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            String sql = "UPDATE origenes SET nombre = ?, descripcion = ? WHERE id = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, origen.getNombre());
                ps.setString(2, origen.getDescripcion());
                ps.setLong(3, origen.getId());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return origen;
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM origenes WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Origen mapResultSet(ResultSet rs) throws SQLException {
        return new Origen(
            rs.getLong("id"),
            rs.getString("nombre"),
            rs.getString("descripcion")
        );
    }
}
