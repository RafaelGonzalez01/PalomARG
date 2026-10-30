package com.palomarg.repository.sqlite;

import com.palomarg.database.DatabaseManager;
import com.palomarg.model.entity.AliasColombofilo;
import com.palomarg.repository.AliasColombofiloRepository;
import com.palomarg.repository.ColombofiloRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteAliasColombofiloRepository implements AliasColombofiloRepository {
    private final ColombofiloRepository colombofiloRepo;

    public SqliteAliasColombofiloRepository() {
        this.colombofiloRepo = new SqliteColombofiloRepository();
    }

    public SqliteAliasColombofiloRepository(ColombofiloRepository colombofiloRepo) {
        this.colombofiloRepo = colombofiloRepo;
    }

    @Override
    public Optional<AliasColombofilo> findById(Long id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT id, nombre_web, nombre_normalizado, codigo, ciudad, id_colombofilo FROM alias_colombofilos WHERE id = ?";
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
    public Optional<AliasColombofilo> findByNombreNormalizado(String nombreNormalizado) {
        if (nombreNormalizado == null) return Optional.empty();
        String sql = "SELECT id, nombre_web, nombre_normalizado, codigo, ciudad, id_colombofilo FROM alias_colombofilos WHERE nombre_normalizado = ?";
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
    public List<AliasColombofilo> findByColombofiloId(Long colombofiloId) {
        List<AliasColombofilo> lista = new ArrayList<>();
        if (colombofiloId == null) return lista;
        String sql = "SELECT id, nombre_web, nombre_normalizado, codigo, ciudad, id_colombofilo FROM alias_colombofilos WHERE id_colombofilo = ? ORDER BY nombre_web";
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
    public List<AliasColombofilo> findAll() {
        List<AliasColombofilo> lista = new ArrayList<>();
        String sql = "SELECT id, nombre_web, nombre_normalizado, codigo, ciudad, id_colombofilo FROM alias_colombofilos ORDER BY nombre_web";
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
    public AliasColombofilo save(AliasColombofilo alias) {
        if (alias.getId() == null) {
            String sql = "INSERT INTO alias_colombofilos (nombre_web, nombre_normalizado, codigo, ciudad, id_colombofilo) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, alias.getNombreWeb());
                ps.setString(2, alias.getNombreNormalizado());
                ps.setString(3, alias.getCodigo());
                ps.setString(4, alias.getCiudad());
                ps.setLong(5, alias.getColombofilo() != null ? alias.getColombofilo().getId() : 0);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) alias.setId(keys.getLong(1));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            String sql = "UPDATE alias_colombofilos SET nombre_web = ?, nombre_normalizado = ?, codigo = ?, ciudad = ?, id_colombofilo = ? WHERE id = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, alias.getNombreWeb());
                ps.setString(2, alias.getNombreNormalizado());
                ps.setString(3, alias.getCodigo());
                ps.setString(4, alias.getCiudad());
                ps.setLong(5, alias.getColombofilo() != null ? alias.getColombofilo().getId() : 0);
                ps.setLong(6, alias.getId());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return alias;
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM alias_colombofilos WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private AliasColombofilo mapResultSet(ResultSet rs) throws SQLException {
        AliasColombofilo a = new AliasColombofilo();
        a.setId(rs.getLong("id"));
        a.setNombreWeb(rs.getString("nombre_web"));
        a.setNombreNormalizado(rs.getString("nombre_normalizado"));
        a.setCodigo(rs.getString("codigo"));
        a.setCiudad(rs.getString("ciudad"));

        Number idCol = (Number) rs.getObject("id_colombofilo");
        if (idCol != null) {
            colombofiloRepo.findById(idCol.longValue()).ifPresent(a::setColombofilo);
        }
        return a;
    }
}
