package com.palomarg.repository.sqlite;

import com.palomarg.database.DatabaseManager;
import com.palomarg.model.entity.Usuario;
import com.palomarg.model.enums.RolUsuario;
import com.palomarg.repository.UsuarioRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteUsuarioRepository implements UsuarioRepository {

    @Override
    public Optional<Usuario> findById(Long id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT id, nombre_usuario, hash_password, rol FROM usuarios WHERE id = ?";
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
    public Optional<Usuario> findByNombreUsuario(String nombreUsuario) {
        if (nombreUsuario == null) return Optional.empty();
        String sql = "SELECT id, nombre_usuario, hash_password, rol FROM usuarios WHERE LOWER(nombre_usuario) = LOWER(?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombreUsuario.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Usuario> findAll() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT id, nombre_usuario, hash_password, rol FROM usuarios ORDER BY nombre_usuario";
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
    public Usuario save(Usuario usuario) {
        if (usuario.getId() == null) {
            String sql = "INSERT INTO usuarios (nombre_usuario, hash_password, rol) VALUES (?, ?, ?)";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, usuario.getNombreUsuario());
                ps.setString(2, usuario.getHashPassword());
                ps.setString(3, usuario.getRol() != null ? usuario.getRol().name() : RolUsuario.USUARIO.name());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) usuario.setId(keys.getLong(1));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            String sql = "UPDATE usuarios SET nombre_usuario = ?, hash_password = ?, rol = ? WHERE id = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, usuario.getNombreUsuario());
                ps.setString(2, usuario.getHashPassword());
                ps.setString(3, usuario.getRol() != null ? usuario.getRol().name() : RolUsuario.USUARIO.name());
                ps.setLong(4, usuario.getId());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return usuario;
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Usuario mapResultSet(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getLong("id"));
        u.setNombreUsuario(rs.getString("nombre_usuario"));
        u.setHashPassword(rs.getString("hash_password"));
        String rolStr = rs.getString("rol");
        if (rolStr != null) {
            try { u.setRol(RolUsuario.valueOf(rolStr)); } catch (Exception ignored) {}
        }
        return u;
    }
}
