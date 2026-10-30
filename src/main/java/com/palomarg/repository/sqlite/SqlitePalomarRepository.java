package com.palomarg.repository.sqlite;

import com.palomarg.database.DatabaseManager;
import com.palomarg.model.entity.Palomar;
import com.palomarg.repository.ColombofiloRepository;
import com.palomarg.repository.PalomarRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqlitePalomarRepository implements PalomarRepository {
    private final ColombofiloRepository colombofiloRepo;

    public SqlitePalomarRepository() {
        this.colombofiloRepo = new SqliteColombofiloRepository();
    }

    public SqlitePalomarRepository(ColombofiloRepository colombofiloRepo) {
        this.colombofiloRepo = colombofiloRepo;
    }

    @Override
    public List<Palomar> findAll() {
        List<Palomar> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, direccion, latitud, longitud, id_colombofilo, propietario FROM palomares ORDER BY nombre";
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
    public Optional<Palomar> findById(Long id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT id, nombre, direccion, latitud, longitud, id_colombofilo, propietario FROM palomares WHERE id = ?";
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
    public List<Palomar> findByColombofiloId(Long colombofiloId) {
        List<Palomar> lista = new ArrayList<>();
        if (colombofiloId == null) return lista;
        String sql = "SELECT id, nombre, direccion, latitud, longitud, id_colombofilo, propietario FROM palomares WHERE id_colombofilo = ? ORDER BY nombre";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, colombofiloId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public Palomar save(Palomar palomar) {
        if (palomar.getId() == null) {
            String sql = "INSERT INTO palomares (nombre, direccion, latitud, longitud, id_colombofilo, propietario) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, palomar.getNombre());
                ps.setString(2, palomar.getDireccion());
                ps.setObject(3, palomar.getLatitud());
                ps.setObject(4, palomar.getLongitud());
                ps.setObject(5, palomar.getColombofilo() != null ? palomar.getColombofilo().getId() : null);
                ps.setString(6, palomar.getPropietario());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        palomar.setId(keys.getLong(1));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            String sql = "UPDATE palomares SET nombre = ?, direccion = ?, latitud = ?, longitud = ?, id_colombofilo = ?, propietario = ? WHERE id = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, palomar.getNombre());
                ps.setString(2, palomar.getDireccion());
                ps.setObject(3, palomar.getLatitud());
                ps.setObject(4, palomar.getLongitud());
                ps.setObject(5, palomar.getColombofilo() != null ? palomar.getColombofilo().getId() : null);
                ps.setString(6, palomar.getPropietario());
                ps.setLong(7, palomar.getId());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return palomar;
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM palomares WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Palomar mapResultSet(ResultSet rs) throws SQLException {
        Palomar p = new Palomar();
        p.setId(rs.getLong("id"));
        p.setNombre(rs.getString("nombre"));
        p.setDireccion(rs.getString("direccion"));

        Number lat = (Number) rs.getObject("latitud");
        if (lat != null) p.setLatitud(lat.doubleValue());

        Number lon = (Number) rs.getObject("longitud");
        if (lon != null) p.setLongitud(lon.doubleValue());

        p.setPropietario(rs.getString("propietario"));

        Number idCol = (Number) rs.getObject("id_colombofilo");
        if (idCol != null) {
            colombofiloRepo.findById(idCol.longValue()).ifPresent(p::setColombofilo);
        }
        return p;
    }
}
