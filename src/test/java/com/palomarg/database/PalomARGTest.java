package com.palomarg.database;

import com.palomarg.model.entity.*;
import com.palomarg.model.enums.*;
import com.palomarg.model.vo.Anillo;
import com.palomarg.model.vo.FechaNac;
import com.palomarg.repository.*;
import com.palomarg.repository.sqlite.*;
import org.junit.jupiter.api.*;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PalomARGTest {
    private static final String TEST_DB = "test_palomarg.db";

    private static UsuarioRepository usuarioRepo;
    private static SociedadRepository sociedadRepo;
    private static ZonaRepository zonaRepo;
    private static ColombofiloRepository colombofiloRepo;
    private static AliasColombofiloRepository aliasRepo;
    private static ReclamoPalomarRepository reclamoRepo;
    private static PalomaRepository palomaRepo;
    private static PalomarRepository palomarRepo;
    private static CarreraRepository carreraRepo;
    private static CampeonatoRepository campeonatoRepo;
    private static OrigenRepository origenRepo;
    private static ParticipacionCarreraRepository partRepo;

    @BeforeAll
    public static void setUp() {
        File dbFile = new File(TEST_DB);
        if (dbFile.exists()) {
            dbFile.delete();
        }

        DatabaseManager.setDbFilePath(TEST_DB);
        DatabaseManager.inicializarBaseDeDatos();
        DatabaseManager.cargarDatosDeEjemploSiEstaVacia();

        usuarioRepo = new SqliteUsuarioRepository();
        sociedadRepo = new SqliteSociedadRepository();
        zonaRepo = new SqliteZonaRepository();
        colombofiloRepo = new SqliteColombofiloRepository(sociedadRepo, zonaRepo, usuarioRepo);
        aliasRepo = new SqliteAliasColombofiloRepository(colombofiloRepo);
        reclamoRepo = new SqliteReclamoPalomarRepository(usuarioRepo, colombofiloRepo);
        origenRepo = new SqliteOrigenRepository();
        palomarRepo = new SqlitePalomarRepository(colombofiloRepo);
        palomaRepo = new SqlitePalomaRepository(origenRepo, palomarRepo, colombofiloRepo);
        carreraRepo = new SqliteCarreraRepository();
        campeonatoRepo = new SqliteCampeonatoRepository(sociedadRepo, carreraRepo);
        partRepo = new SqliteParticipacionCarreraRepository(palomaRepo, carreraRepo, colombofiloRepo);
    }

    @AfterAll
    public static void tearDown() {
        File dbFile = new File(TEST_DB);
        if (dbFile.exists()) {
            dbFile.delete();
        }
    }

    @Test
    @Order(1)
    public void testDatosInicialesCargados() {
        List<Colombofilo> colombofilos = colombofiloRepo.findAll();
        assertFalse(colombofilos.isEmpty(), "Deben existir colombófilos cargados");
        assertTrue(colombofilos.stream().anyMatch(c -> "don alberto rossi".equals(c.getNombreNormalizado())));

        List<Paloma> palomas = palomaRepo.findAll();
        assertFalse(palomas.isEmpty(), "Deben haberse cargado palomas de ejemplo");
        assertTrue(palomas.size() >= 5);

        List<Palomar> palomares = palomarRepo.findAll();
        assertFalse(palomares.isEmpty(), "Debe existir al menos un palomar");
        assertNotNull(palomares.get(0).getColombofilo());

        List<Carrera> carreras = carreraRepo.findAll();
        assertFalse(carreras.isEmpty(), "Deben existir carreras registradas");
        assertNotNull(carreras.get(0).getRefKm());
    }

    @Test
    @Order(2)
    public void testModeloUsuarioYSociedadYZona() {
        Usuario user = new Usuario("competidor1", "hashSeguro123", RolUsuario.USUARIO);
        usuarioRepo.save(user);
        assertNotNull(user.getId());

        Sociedad soc = new Sociedad("Sociedad Colombófila Rosario", "Rosario", "SCR-03");
        sociedadRepo.save(soc);
        assertNotNull(soc.getId());

        Zona zona = new Zona("Zona Oeste", "Sector suburbano oeste");
        zonaRepo.save(zona);
        assertNotNull(zona.getId());

        Colombofilo col = new Colombofilo("Juan Manuel Fangio", "Rosario");
        col.setCodigo("305");
        col.setSociedad(soc);
        col.setZona(zona);
        col.setUsuario(user);
        col.setExternalId("EXT-COL-305");
        col.setFuente("SincroWeb");
        col.setUltimaSincronizacion(LocalDateTime.now());
        colombofiloRepo.save(col);

        assertNotNull(col.getId());
        assertEquals("[305]", col.getCodigo());
        assertEquals("juan manuel fangio", col.getNombreNormalizado());
    }

    @Test
    @Order(3)
    public void testAliasYReclamoPalomar() {
        Colombofilo col = colombofiloRepo.findByExternalId("EXT-COL-305").orElseThrow();
        Usuario user = usuarioRepo.findByNombreUsuario("competidor1").orElseThrow();

        AliasColombofilo alias = new AliasColombofilo("Fangio Juan M [305]", "Rosario", col);
        aliasRepo.save(alias);
        assertNotNull(alias.getId());
        assertEquals("fangio juan m 305", alias.getNombreNormalizado());

        ReclamoPalomar reclamo = new ReclamoPalomar(user, col, "Solicito vinculación con mi palomar oficial");
        reclamoRepo.save(reclamo);
        assertNotNull(reclamo.getId());
        assertEquals(EstadoReclamo.PENDIENTE, reclamo.getEstado());
    }

    @Test
    @Order(4)
    public void testPalomaConAnilloCompuestoYPropietario() {
        Colombofilo col = colombofiloRepo.findByExternalId("EXT-COL-305").orElseThrow();

        // Paloma estándar con anillo ARG-FCA-24-555888
        Anillo anillo = new Anillo("ARG", "FCA", 24, "555888");
        Paloma nueva = new Paloma(anillo, col);
        nueva.setSexo(SexoPaloma.HEMBRA);
        nueva.setColor(Colores.ROJO);
        nueva.setNacimiento(new FechaNac(2024));
        nueva.setExternalId("EXT-PAL-555888");
        palomaRepo.save(nueva);

        Optional<Paloma> encontrada = palomaRepo.findByAnilloCompuesto("ARG", "FCA", 24, "555888");
        assertTrue(encontrada.isPresent());
        assertEquals("ARG-FCA-24-555888", encontrada.get().getNumeroAnillo());
        assertEquals(col.getId(), encontrada.get().getPropietario().getId());
        assertEquals(SexoPaloma.HEMBRA, encontrada.get().getSexo());

        // Paloma importada: sin sexo, sin color, sin fecha de nacimiento (todo opcional)
        Anillo anilloImportada = new Anillo("NL", "NPO", 23, "777000");
        Paloma importada = new Paloma(anilloImportada, col);
        importada.setSexo(null);
        importada.setColor(null);
        importada.setNacimiento(null);
        palomaRepo.save(importada);

        Optional<Paloma> impEnc = palomaRepo.findByNumeroAnillo("NL-NPO-23-777000");
        assertTrue(impEnc.isPresent());
        assertNull(impEnc.get().getSexo());
        assertNull(impEnc.get().getColor());
        assertNull(impEnc.get().getNacimiento());
    }

    @Test
    @Order(5)
    public void testCarreraEncestesYPorcentajeMarcadas() {
        LocalDateTime suelta = LocalDateTime.of(2026, 11, 10, 7, 0, 0);
        Carrera c = new Carrera("Carrera Especial Rosario", suelta, 350.0, "Despejado", "Viento en popa 15 km/h", TipoCategoria.MEDIO_FONDO);
        c.setCiudad("Rosario");
        c.setPuntoSuelta("Parque Independencia");
        c.setLatitudSuelta(-32.9555);
        c.setLongitudSuelta(-60.6666);
        c.setIdWeb("WEB-ROS-2026-03");
        c.setEncestes(100); // 100 palomas encestadas
        c.setExternalId("EXT-CAR-03");
        carreraRepo.save(c);

        assertNotNull(c.getId());
        assertEquals(0, c.getMarcadas());
        assertEquals(0.0, c.getPorcentajeMarcadas());

        // Agregar 2 participaciones marcadas
        Paloma p1 = palomaRepo.findByNumeroAnillo("ARG-FCA-24-555888").orElseThrow();
        Colombofilo col = p1.getPropietario();

        LocalDateTime llegada = suelta.plusHours(4).plusMinutes(30); // 4.5 horas
        ParticipacionCarrera part1 = new ParticipacionCarrera(p1, c, llegada);
        part1.setColombofilo(col);
        partRepo.save(part1);

        c.setParticipaciones(partRepo.findByCarreraId(c.getId()));
        assertEquals(1, c.getMarcadas());
        assertEquals(1.0, c.getPorcentajeMarcadas(), 0.01); // 1 de 100 = 1%
    }

    @Test
    @Order(6)
    public void testParticipacionCarreraTiempoVueloMayor24hYVelocidades() {
        Carrera c = carreraRepo.findByIdWeb("WEB-ROS-2026-03").orElseThrow();
        Paloma p = palomaRepo.findByNumeroAnillo("ARG-FCA-24-555888").orElseThrow();

        // Simular llegada de gran fondo al día siguiente (26 horas y 30 minutos después)
        LocalDateTime suelta = c.getFechaHoraSuelta();
        LocalDateTime llegadaLarga = suelta.plusDays(1).plusHours(2).plusMinutes(30);

        ParticipacionCarrera part = new ParticipacionCarrera(p, c, llegadaLarga);
        part.setColombofilo(p.getPropietario());
        part.setMetodoMarcada("Electrónico Tauris");
        part.setEquipo("Equipo A");
        part.getZonas().add("Zona Oeste");

        Duration vuelo = part.getTiempoVuelo();
        assertNotNull(vuelo);
        assertTrue(vuelo.toHours() >= 26, "El tiempo de vuelo debe superar las 24 horas");
        assertEquals("26h 30m 00s", part.getTiempoVueloFormateado());

        // 350 km en 26.5 horas = 1590 minutos -> 350,000 / 1590 = 220.13 m/m
        assertEquals(220.13, part.getVelocidadMetrosMin(), 0.05);
        assertEquals(13.21, part.getVelocidadKmh(), 0.05);

        partRepo.save(part);
        assertNotNull(part.getId());
    }

    @Test
    @Order(7)
    public void testCampeonatoMuchosAMuchosYPropiedades() {
        Sociedad soc = sociedadRepo.findByNombre("Sociedad Colombófila Rosario").orElseThrow();
        Carrera c1 = carreraRepo.findByIdWeb("WEB-ROS-2026-03").orElseThrow();
        Carrera c2 = carreraRepo.findById(1L).orElseThrow();

        Campeonato camp = new Campeonato("Campeonato Interprovincial", 2026, soc, AlcanceCampeonato.REGIONAL, "Class A", CategoriaCampeonato.FONDO);
        camp.setExternalId("EXT-CAMP-2026-REG");
        camp.agregarCarrera(c1);
        camp.agregarCarrera(c2);
        campeonatoRepo.save(camp);

        assertNotNull(camp.getId());
        List<Carrera> asociadas = campeonatoRepo.findCarrerasByCampeonatoId(camp.getId());
        assertEquals(2, asociadas.size(), "Debe tener 2 carreras vinculadas en la tabla intermedia");
    }

    @Test
    @Order(8)
    public void testEliminarPaloma() {
        boolean borrado = palomaRepo.deleteByNumeroAnillo("ARG-FCA-24-555888");
        assertTrue(borrado);
        Optional<Paloma> postBorrado = palomaRepo.findByNumeroAnillo("ARG-FCA-24-555888");
        assertTrue(postBorrado.isEmpty());
    }
}
