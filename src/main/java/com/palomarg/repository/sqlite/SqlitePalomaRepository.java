package com.palomarg.repository.sqlite;

import com.palomarg.database.DatabaseManager;
import com.palomarg.model.entity.Colombofilo;
import com.palomarg.model.entity.Paloma;
import com.palomarg.model.enums.Colores;
import com.palomarg.model.enums.RolPaloma;
import com.palomarg.model.enums.SexoPaloma;
import com.palomarg.model.vo.Anillo;
import com.palomarg.model.vo.FechaNac;
import com.palomarg.repository.ColombofiloRepository;
import com.palomarg.repository.OrigenRepository;
import com.palomarg.repository.PalomaRepository;
import com.palomarg.repository.PalomarRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqlitePalomaRepository implements PalomaRepository {
    private final OrigenRepository origenRepo;
    private final PalomarRepository palomarRepo;
    private final ColombofiloRepository colombofiloRepo;

    public SqlitePalomaRepository() {
        this.origenRepo = new SqliteOrigenRepository();
        this.palomarRepo = new SqlitePalomarRepository();
        this.colombofiloRepo = new SqliteColombofiloRepository();
    }

    public SqlitePalomaRepository(OrigenRepository origenRepo, PalomarRepository palomarRepo, ColombofiloRepository colombofiloRepo) {
        this.origenRepo = origenRepo;
        this.palomarRepo = palomarRepo;
        this.colombofiloRepo = colombofiloRepo;
    }

    @Override
    public List<Paloma> findAll() {
        List<Paloma> lista = new ArrayList<>();
        String sql = """
            SELECT p.id, p.pais, p.institucion, p.anio, p.numero,
                   p.id_propietario, p.id_palomar, p.id_origen,
                   p.padre_id, p.madre_id, p.padre_anillo, p.madre_anillo,
                   p.sexo, p.color, p.rol, p.anio_nac, p.mes_nac, p.dia_nac,
                   p.external_id, p.fuente, p.ultima_sincronizacion
            FROM palomas p
            ORDER BY p.institucion, p.numero
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
    public Optional<Paloma> findById(Long id) {
        if (id == null) return Optional.empty();
        String sql = """
            SELECT p.id, p.pais, p.institucion, p.anio, p.numero,
                   p.id_propietario, p.id_palomar, p.id_origen,
                   p.padre_id, p.madre_id, p.padre_anillo, p.madre_anillo,
                   p.sexo, p.color, p.rol, p.anio_nac, p.mes_nac, p.dia_nac,
                   p.external_id, p.fuente, p.ultima_sincronizacion
            FROM palomas p
            WHERE p.id = ?
        """;
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
    public Optional<Paloma> findByNumeroAnillo(String textoAnillo) {
        if (textoAnillo == null || textoAnillo.trim().isEmpty()) return Optional.empty();
        Anillo parsed = Anillo.parse(textoAnillo);
        if (parsed != null && parsed.getNumero() != null) {
            return findByAnilloCompuesto(parsed.getPais(), parsed.getInstitucion(), parsed.getAnio(), parsed.getNumero());
        }

        // Búsqueda directa por número simple
        String sql = "SELECT p.id, p.pais, p.institucion, p.anio, p.numero, p.id_propietario, p.id_palomar, p.id_origen, p.padre_id, p.madre_id, p.padre_anillo, p.madre_anillo, p.sexo, p.color, p.rol, p.anio_nac, p.mes_nac, p.dia_nac, p.external_id, p.fuente, p.ultima_sincronizacion FROM palomas p WHERE p.numero = ? LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, textoAnillo.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<Paloma> findByAnilloCompuesto(String pais, String institucion, Integer anio, String numero) {
        if (numero == null) return Optional.empty();
        StringBuilder sql = new StringBuilder("""
            SELECT p.id, p.pais, p.institucion, p.anio, p.numero,
                   p.id_propietario, p.id_palomar, p.id_origen,
                   p.padre_id, p.madre_id, p.padre_anillo, p.madre_anillo,
                   p.sexo, p.color, p.rol, p.anio_nac, p.mes_nac, p.dia_nac,
                   p.external_id, p.fuente, p.ultima_sincronizacion
            FROM palomas p
            WHERE p.numero = ?
        """);

        List<Object> params = new ArrayList<>();
        params.add(numero.trim());

        if (institucion != null) {
            sql.append(" AND p.institucion = ?");
            params.add(institucion.trim().toUpperCase());
        }
        if (anio != null) {
            sql.append(" AND p.anio = ?");
            params.add(anio);
        }
        if (pais != null) {
            sql.append(" AND p.pais = ?");
            params.add(pais.trim().toUpperCase());
        }

        sql.append(" LIMIT 1");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<Paloma> findByExternalId(String externalId) {
        if (externalId == null) return Optional.empty();
        String sql = "SELECT p.id, p.pais, p.institucion, p.anio, p.numero, p.id_propietario, p.id_palomar, p.id_origen, p.padre_id, p.madre_id, p.padre_anillo, p.madre_anillo, p.sexo, p.color, p.rol, p.anio_nac, p.mes_nac, p.dia_nac, p.external_id, p.fuente, p.ultima_sincronizacion FROM palomas p WHERE p.external_id = ?";
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
    public List<Paloma> findByPropietarioId(Long propietarioId) {
        List<Paloma> lista = new ArrayList<>();
        if (propietarioId == null) return lista;
        String sql = "SELECT p.id, p.pais, p.institucion, p.anio, p.numero, p.id_propietario, p.id_palomar, p.id_origen, p.padre_id, p.madre_id, p.padre_anillo, p.madre_anillo, p.sexo, p.color, p.rol, p.anio_nac, p.mes_nac, p.dia_nac, p.external_id, p.fuente, p.ultima_sincronizacion FROM palomas p WHERE p.id_propietario = ? ORDER BY p.numero";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, propietarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public List<Paloma> search(String filtroAnillo, SexoPaloma filtroSexo, RolPaloma filtroRol) {
        List<Paloma> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT p.id, p.pais, p.institucion, p.anio, p.numero,
                   p.id_propietario, p.id_palomar, p.id_origen,
                   p.padre_id, p.madre_id, p.padre_anillo, p.madre_anillo,
                   p.sexo, p.color, p.rol, p.anio_nac, p.mes_nac, p.dia_nac,
                   p.external_id, p.fuente, p.ultima_sincronizacion
            FROM palomas p
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();

        if (filtroAnillo != null && !filtroAnillo.trim().isEmpty()) {
            sql.append(" AND (p.numero LIKE ? OR (p.pais || '-' || p.institucion || '-' || p.numero) LIKE ?)");
            String q = "%" + filtroAnillo.trim().toUpperCase() + "%";
            params.add(q);
            params.add(q);
        }
        if (filtroSexo != null) {
            sql.append(" AND p.sexo = ?");
            params.add(filtroSexo.name());
        }
        if (filtroRol != null) {
            sql.append(" AND p.rol = ?");
            params.add(filtroRol.name());
        }

        sql.append(" ORDER BY p.institucion, p.numero");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
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
    public Paloma save(Paloma paloma) {
        if (paloma == null || paloma.getAnillo() == null || paloma.getNumeroSerieAnillo().isEmpty()) {
            throw new IllegalArgumentException("El número de anillo es obligatorio.");
        }

        // Si no tiene propietario explícito asignado, vincular al primer colombófilo existente
        if (paloma.getPropietario() == null) {
            List<Colombofilo> col = colombofiloRepo.findAll();
            if (!col.isEmpty()) {
                paloma.setPropietario(col.get(0));
            } else {
                Colombofilo nuevo = new Colombofilo("Don Alberto Rossi", "Pergamino");
                colombofiloRepo.save(nuevo);
                paloma.setPropietario(nuevo);
            }
        }

        Anillo an = paloma.getAnillo();
        Optional<Paloma> existente = paloma.getId() != null ? findById(paloma.getId()) :
                findByAnilloCompuesto(an.getPais(), an.getInstitucion(), an.getAnio(), an.getNumero());

        if (existente.isEmpty()) {
            String sql = """
                INSERT INTO palomas (pais, institucion, anio, numero, id_propietario, id_palomar, id_origen,
                                     padre_id, madre_id, padre_anillo, madre_anillo, sexo, color, rol,
                                     anio_nac, mes_nac, dia_nac, external_id, fuente, ultima_sincronizacion)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                setParameters(ps, paloma);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) paloma.setId(keys.getLong(1));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            paloma.setId(existente.get().getId());
            String sql = """
                UPDATE palomas SET pais = ?, institucion = ?, anio = ?, numero = ?, id_propietario = ?, id_palomar = ?, id_origen = ?,
                                   padre_id = ?, madre_id = ?, padre_anillo = ?, madre_anillo = ?, sexo = ?, color = ?, rol = ?,
                                   anio_nac = ?, mes_nac = ?, dia_nac = ?, external_id = ?, fuente = ?, ultima_sincronizacion = ?
                WHERE id = ?
            """;
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                setParameters(ps, paloma);
                ps.setLong(21, paloma.getId());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return paloma;
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM palomas WHERE id = ?";
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
    public boolean deleteByNumeroAnillo(String anillo) {
        Optional<Paloma> p = findByNumeroAnillo(anillo);
        return p.filter(paloma -> deleteById(paloma.getId())).isPresent();
    }

    @Override
    public int countTotal() {
        String sql = "SELECT COUNT(*) FROM palomas";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void setParameters(PreparedStatement ps, Paloma paloma) throws SQLException {
        Anillo an = paloma.getAnillo();
        FechaNac fn = paloma.getNacimiento();

        ps.setString(1, an != null ? an.getPais() : "ARG");
        ps.setString(2, an != null ? an.getInstitucion() : "FCA");
        ps.setObject(3, an != null ? an.getAnio() : null);
        ps.setString(4, an != null ? an.getNumero() : "");
        ps.setLong(5, paloma.getPropietario() != null ? paloma.getPropietario().getId() : 1);
        ps.setObject(6, paloma.getPalomar() != null ? paloma.getPalomar().getId() : null);
        ps.setObject(7, paloma.getOrigen() != null ? paloma.getOrigen().getId() : null);
        ps.setObject(8, paloma.getPadre() != null ? paloma.getPadre().getId() : null);
        ps.setObject(9, paloma.getMadre() != null ? paloma.getMadre().getId() : null);
        ps.setString(10, paloma.getPadreAnillo());
        ps.setString(11, paloma.getMadreAnillo());
        ps.setString(12, paloma.getSexo() != null ? paloma.getSexo().name() : null);
        ps.setString(13, paloma.getColor() != null ? paloma.getColor().name() : null);
        ps.setString(14, paloma.getRol() != null ? paloma.getRol().name() : RolPaloma.VOLADORA.name());
        ps.setObject(15, fn != null ? fn.getAnio() : null);
        ps.setObject(16, fn != null ? fn.getMes() : null);
        ps.setObject(17, fn != null ? fn.getDia() : null);
        ps.setString(18, paloma.getExternalId());
        ps.setString(19, paloma.getFuente());
        ps.setString(20, paloma.getUltimaSincronizacion() != null ? paloma.getUltimaSincronizacion().toString() : null);
    }

    private Paloma mapResultSet(ResultSet rs) throws SQLException {
        Paloma p = new Paloma();
        p.setId(rs.getLong("id"));

        String pais = rs.getString("pais");
        String inst = rs.getString("institucion");
        Integer anio = (Integer) rs.getObject("anio");
        String numero = rs.getString("numero");
        p.setAnillo(new Anillo(pais, inst, anio, numero));

        Number idProp = (Number) rs.getObject("id_propietario");
        if (idProp != null) {
            colombofiloRepo.findById(idProp.longValue()).ifPresent(p::setPropietario);
        }

        Number idPalomar = (Number) rs.getObject("id_palomar");
        if (idPalomar != null) {
            palomarRepo.findById(idPalomar.longValue()).ifPresent(p::setPalomar);
        }

        Number idOrigen = (Number) rs.getObject("id_origen");
        if (idOrigen != null) {
            origenRepo.findById(idOrigen.longValue()).ifPresent(p::setOrigen);
        }

        String padreAnillo = rs.getString("padre_anillo");
        if (padreAnillo != null) {
            Paloma padreRef = new Paloma();
            padreRef.setNumeroAnillo(padreAnillo);
            p.setPadre(padreRef);
        }

        String madreAnillo = rs.getString("madre_anillo");
        if (madreAnillo != null) {
            Paloma madreRef = new Paloma();
            madreRef.setNumeroAnillo(madreAnillo);
            p.setMadre(madreRef);
        }

        String sexoStr = rs.getString("sexo");
        if (sexoStr != null) {
            try { p.setSexo(SexoPaloma.valueOf(sexoStr)); } catch (Exception ignored) {}
        }

        String colorStr = rs.getString("color");
        if (colorStr != null) {
            try { p.setColor(Colores.valueOf(colorStr)); } catch (Exception ignored) {}
        }

        String rolStr = rs.getString("rol");
        if (rolStr != null) {
            try { p.setRol(RolPaloma.valueOf(rolStr)); } catch (Exception ignored) {}
        }

        Integer anioNac = (Integer) rs.getObject("anio_nac");
        Integer mesNac = (Integer) rs.getObject("mes_nac");
        Integer diaNac = (Integer) rs.getObject("dia_nac");
        if (anioNac != null) {
            p.setNacimiento(new FechaNac(diaNac, mesNac, anioNac));
        }

        p.setExternalId(rs.getString("external_id"));
        p.setFuente(rs.getString("fuente"));

        String ultSinc = rs.getString("ultima_sincronizacion");
        if (ultSinc != null) {
            try { p.setUltimaSincronizacion(LocalDateTime.parse(ultSinc)); } catch (Exception ignored) {}
        }

        return p;
    }
}
