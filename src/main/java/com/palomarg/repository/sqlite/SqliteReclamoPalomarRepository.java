package com.palomarg.repository.sqlite;

import com.palomarg.database.DatabaseManager;
import com.palomarg.model.entity.ReclamoPalomar;
import com.palomarg.model.enums.EstadoReclamo;
import com.palomarg.repository.ColombofiloRepository;
import com.palomarg.repository.ReclamoPalomarRepository;
import com.palomarg.repository.UsuarioRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteReclamoPalomarRepository implements ReclamoPalomarRepository {
    private final UsuarioRepository usuarioRepo;
    private final ColombofiloRepository colombofiloRepo;

    public SqliteReclamoPalomarRepository() {
        this.usuarioRepo = new SqliteUsuarioRepository();
        this.colombofiloRepo = new SqliteColombofiloRepository();
    }

    public SqliteReclamoPalomarRepository(UsuarioRepository usuarioRepo, ColombofiloRepository colombofiloRepo) {
        this.usuarioRepo = usuarioRepo;
        this.colombofiloRepo = colombofiloRepo;
    }

    @Override
    public Optional<ReclamoPalomar> findById(Long id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT id, id_usuario, id_colombofilo, estado, fecha, comentario FROM reclamos_palomar WHERE id = ?";
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
    public List<ReclamoPalomar> findByUsuarioId(Long usuarioId) {
        List<ReclamoPalomar> lista = new ArrayList<>();
        if (usuarioId == null) return lista;
        String sql = "SELECT id, id_usuario, id_colombofilo, estado, fecha, comentario FROM reclamos_palomar WHERE id_usuario = ? ORDER BY fecha DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public List<ReclamoPalomar> findByEstado(EstadoReclamo estado) {
        List<ReclamoPalomar> lista = new ArrayList<>();
        if (estado == null) return lista;
        String sql = "SELECT id, id_usuario, id_colombofilo, estado, fecha, comentario FROM reclamos_palomar WHERE estado = ? ORDER BY fecha DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, estado.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public List<ReclamoPalomar> findAll() {
        List<ReclamoPalomar> lista = new ArrayList<>();
        String sql = "SELECT id, id_usuario, id_colombofilo, estado, fecha, comentario FROM reclamos_palomar ORDER BY fecha DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapResultSet(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public ReclamoPalomar save(ReclamoPalomar r) {
        if (r.getId() == null) {
            String sql = "INSERT INTO reclamos_palomar (id_usuario, id_colombofilo, estado, fecha, comentario) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, r.getUsuario() != null ? r.getUsuario().getId() : 0);
                ps.setLong(2, r.getColombofilo() != null ? r.getColombofilo().getId() : 0);
                ps.setString(3, r.getEstado() != null ? r.getEstado().name() : EstadoReclamo.PENDIENTE.name());
                ps.setString(4, r.getFecha() != null ? r.getFecha().toString() : LocalDateTime.now().toString());
                ps.setString(5, r.getComentario());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) r.setId(keys.getLong(1));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            String sql = "UPDATE reclamos_palomar SET id_usuario = ?, id_colombofilo = ?, estado = ?, fecha = ?, comentario = ? WHERE id = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, r.getUsuario() != null ? r.getUsuario().getId() : 0);
                ps.setLong(2, r.getColombofilo() != null ? r.getColombofilo().getId() : 0);
                ps.setString(3, r.getEstado() != null ? r.getEstado().name() : EstadoReclamo.PENDIENTE.name());
                ps.setString(4, r.getFecha() != null ? r.getFecha().toString() : LocalDateTime.now().toString());
                ps.setString(5, r.getComentario());
                ps.setLong(6, r.getId());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return r;
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM reclamos_palomar WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private ReclamoPalomar mapResultSet(ResultSet rs) throws SQLException {
        ReclamoPalomar r = new ReclamoPalomar();
        r.setId(rs.getLong("id"));

        Number idUsu = (Number) rs.getObject("id_usuario");
        if (idUsu != null) usuarioRepo.findById(idUsu.longValue()).ifPresent(r::setUsuario);

        Number idCol = (Number) rs.getObject("id_colombofilo");
        if (idCol != null) colombofiloRepo.findById(idCol.longValue()).ifPresent(r::setColombofilo);

        String estStr = rs.getString("estado");
        if (estStr != null) {
            try { r.setEstado(EstadoReclamo.valueOf(estStr)); } catch (Exception ignored) {}
        }

        String fechaStr = rs.getString("fecha");
        if (fechaStr != null) {
            try { r.setFecha(LocalDateTime.parse(fechaStr)); } catch (Exception ignored) {}
        }

        r.setComentario(rs.getString("comentario"));
        return r;
    }
}
