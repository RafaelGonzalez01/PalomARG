package com.palomarg.database;

import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Gestor de conexión y esquema de base de datos SQLite para PalomARG.
 * Configura las tablas normalizadas con claves compuestas, relaciones y restricciones.
 */
public class DatabaseManager {
    private static final String DEFAULT_DB_FILE = "palomarg.db";
    private static String dbFilePath = DEFAULT_DB_FILE;

    public static synchronized void setDbFilePath(String path) {
        dbFilePath = path;
    }

    public static String getDbFilePath() {
        return dbFilePath;
    }

    public static Connection getConnection() throws SQLException {
        String url = "jdbc:sqlite:" + dbFilePath;
        Connection conn = DriverManager.getConnection(url);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    /**
     * Inicializa todas las tablas de la base de datos según el modelo.
     */
    public static void inicializarBaseDeDatos() {
        String sqlUsuarios = """
            CREATE TABLE IF NOT EXISTS usuarios (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre_usuario TEXT NOT NULL UNIQUE,
                hash_password TEXT NOT NULL,
                rol TEXT NOT NULL
            );
        """;

        String sqlSociedades = """
            CREATE TABLE IF NOT EXISTS sociedades (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL UNIQUE,
                localidad TEXT,
                codigo TEXT
            );
        """;

        String sqlZonas = """
            CREATE TABLE IF NOT EXISTS zonas (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL UNIQUE,
                descripcion TEXT
            );
        """;

        String sqlColombofilos = """
            CREATE TABLE IF NOT EXISTS colombofilos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                nombre_normalizado TEXT NOT NULL,
                codigo TEXT,
                localidad TEXT,
                id_sociedad INTEGER,
                id_zona INTEGER,
                id_usuario INTEGER,
                external_id TEXT UNIQUE,
                fuente TEXT,
                ultima_sincronizacion TEXT,
                FOREIGN KEY (id_sociedad) REFERENCES sociedades(id) ON DELETE SET NULL,
                FOREIGN KEY (id_zona) REFERENCES zonas(id) ON DELETE SET NULL,
                FOREIGN KEY (id_usuario) REFERENCES usuarios(id) ON DELETE SET NULL
            );
        """;

        String sqlAliasColombofilos = """
            CREATE TABLE IF NOT EXISTS alias_colombofilos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre_web TEXT NOT NULL,
                nombre_normalizado TEXT NOT NULL,
                codigo TEXT,
                ciudad TEXT,
                id_colombofilo INTEGER NOT NULL,
                FOREIGN KEY (id_colombofilo) REFERENCES colombofilos(id) ON DELETE CASCADE
            );
        """;

        String sqlReclamos = """
            CREATE TABLE IF NOT EXISTS reclamos_palomar (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                id_usuario INTEGER NOT NULL,
                id_colombofilo INTEGER NOT NULL,
                estado TEXT NOT NULL,
                fecha TEXT NOT NULL,
                comentario TEXT,
                FOREIGN KEY (id_usuario) REFERENCES usuarios(id) ON DELETE CASCADE,
                FOREIGN KEY (id_colombofilo) REFERENCES colombofilos(id) ON DELETE CASCADE
            );
        """;

        String sqlPalomares = """
            CREATE TABLE IF NOT EXISTS palomares (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                direccion TEXT,
                latitud REAL,
                longitud REAL,
                id_colombofilo INTEGER,
                propietario TEXT,
                FOREIGN KEY (id_colombofilo) REFERENCES colombofilos(id) ON DELETE SET NULL
            );
        """;

        String sqlOrigenes = """
            CREATE TABLE IF NOT EXISTS origenes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL UNIQUE,
                descripcion TEXT
            );
        """;

        String sqlPalomas = """
            CREATE TABLE IF NOT EXISTS palomas (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                pais TEXT NOT NULL DEFAULT 'ARG',
                institucion TEXT NOT NULL DEFAULT 'FCA',
                anio INTEGER,
                numero TEXT NOT NULL,
                id_propietario INTEGER NOT NULL,
                id_palomar INTEGER,
                id_origen INTEGER,
                padre_id INTEGER,
                madre_id INTEGER,
                padre_anillo TEXT,
                madre_anillo TEXT,
                sexo TEXT,
                color TEXT,
                rol TEXT,
                anio_nac INTEGER,
                mes_nac INTEGER,
                dia_nac INTEGER,
                external_id TEXT UNIQUE,
                fuente TEXT,
                ultima_sincronizacion TEXT,
                UNIQUE (pais, institucion, anio, numero),
                FOREIGN KEY (id_propietario) REFERENCES colombofilos(id) ON DELETE RESTRICT,
                FOREIGN KEY (id_palomar) REFERENCES palomares(id) ON DELETE SET NULL,
                FOREIGN KEY (id_origen) REFERENCES origenes(id) ON DELETE SET NULL,
                FOREIGN KEY (padre_id) REFERENCES palomas(id) ON DELETE SET NULL,
                FOREIGN KEY (madre_id) REFERENCES palomas(id) ON DELETE SET NULL
            );
        """;

        String sqlCampeonatos = """
            CREATE TABLE IF NOT EXISTS campeonatos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                temporada INTEGER NOT NULL,
                id_sociedad INTEGER,
                alcance TEXT NOT NULL,
                clase TEXT NOT NULL,
                categoria TEXT NOT NULL,
                external_id TEXT UNIQUE,
                fuente TEXT,
                ultima_sincronizacion TEXT,
                FOREIGN KEY (id_sociedad) REFERENCES sociedades(id) ON DELETE SET NULL
            );
        """;

        String sqlCarreras = """
            CREATE TABLE IF NOT EXISTS carreras (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                fecha_hora_suelta TEXT NOT NULL,
                ciudad TEXT,
                punto_suelta TEXT,
                latitud_suelta REAL,
                longitud_suelta REAL,
                clima TEXT,
                viento TEXT,
                ref_km REAL,
                puntos_primera REAL,
                id_web TEXT UNIQUE,
                encestes INTEGER,
                tipo_categoria TEXT,
                external_id TEXT UNIQUE,
                fuente TEXT,
                ultima_sincronizacion TEXT
            );
        """;

        String sqlCampeonatoCarreras = """
            CREATE TABLE IF NOT EXISTS campeonato_carreras (
                id_campeonato INTEGER,
                id_carrera INTEGER,
                PRIMARY KEY (id_campeonato, id_carrera),
                FOREIGN KEY (id_campeonato) REFERENCES campeonatos(id) ON DELETE CASCADE,
                FOREIGN KEY (id_carrera) REFERENCES carreras(id) ON DELETE CASCADE
            );
        """;

        String sqlParticipaciones = """
            CREATE TABLE IF NOT EXISTS participaciones (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                carrera_id INTEGER NOT NULL,
                paloma_id INTEGER,
                paloma_anillo TEXT NOT NULL,
                id_colombofilo INTEGER,
                puesto INTEGER,
                puntos REAL,
                hora_llegada TEXT,
                segundos_vuelo INTEGER,
                velocidad_metros_min REAL,
                velocidad_kmh REAL,
                distancia_km REAL,
                equipo TEXT,
                zonas TEXT,
                region TEXT,
                rango TEXT,
                hora_marcada TEXT,
                metodo_marcada TEXT,
                categoria_edad TEXT,
                FOREIGN KEY (carrera_id) REFERENCES carreras(id) ON DELETE CASCADE,
                FOREIGN KEY (paloma_id) REFERENCES palomas(id) ON DELETE CASCADE,
                FOREIGN KEY (id_colombofilo) REFERENCES colombofilos(id) ON DELETE SET NULL
            );
        """;

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sqlUsuarios);
            stmt.execute(sqlSociedades);
            stmt.execute(sqlZonas);
            stmt.execute(sqlColombofilos);
            stmt.execute(sqlAliasColombofilos);
            stmt.execute(sqlReclamos);
            stmt.execute(sqlPalomares);
            stmt.execute(sqlOrigenes);
            stmt.execute(sqlPalomas);
            stmt.execute(sqlCampeonatos);
            stmt.execute(sqlCarreras);
            stmt.execute(sqlCampeonatoCarreras);
            stmt.execute(sqlParticipaciones);
            System.out.println("Base de datos SQLite inicializada en: " + new File(dbFilePath).getAbsolutePath());
        } catch (SQLException e) {
            System.err.println("Error al inicializar la base de datos SQLite: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Carga datos iniciales de ejemplo si la base está vacía.
     */
    public static void cargarDatosDeEjemploSiEstaVacia() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            var rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM colombofilos");
            if (rs.next() && rs.getInt("total") == 0) {
                System.out.println("Cargando datos iniciales enriquecidos para colombofilia...");

                // Usuarios con contraseñas hasheadas con BCrypt
                String hashMaster = BCrypt.hashpw("master123", BCrypt.gensalt());
                String hashAlberto = BCrypt.hashpw("alberto123", BCrypt.gensalt());
                stmt.execute("INSERT INTO usuarios (nombre_usuario, hash_password, rol) VALUES " +
                    "('master', '" + hashMaster + "', 'MASTER')," +
                    "('alberto', '" + hashAlberto + "', 'USUARIO')");


                // Sociedades y Zonas
                stmt.execute("""
                    INSERT INTO sociedades (nombre, localidad, codigo) VALUES 
                    ('Sociedad Colombófila Pergamino', 'Pergamino', 'SCP-01'),
                    ('Sociedad Colombófila La Plata', 'La Plata', 'SCLP-02');
                """);

                stmt.execute("""
                    INSERT INTO zonas (nombre, descripcion) VALUES 
                    ('Zona Norte', 'Zonas del corredor norte de la provincia'),
                    ('Zona Centro', 'Zonas urbanas y centro'),
                    ('Zona Sur', 'Corredor sur y costa');
                """);

                // Colombófilos
                stmt.execute("""
                    INSERT INTO colombofilos (nombre, nombre_normalizado, codigo, localidad, id_sociedad, id_zona, id_usuario) VALUES 
                    ('Don Alberto Rossi', 'don alberto rossi', '[101]', 'Pergamino', 1, 1, 2),
                    ('Carlos Gómez', 'carlos gomez', '[102]', 'Pergamino', 1, 1, NULL),
                    ('Esteban Martínez', 'esteban martinez', '[205]', 'La Plata', 2, 3, NULL);
                """);

                // Alias
                stmt.execute("""
                    INSERT INTO alias_colombofilos (nombre_web, nombre_normalizado, codigo, ciudad, id_colombofilo) VALUES 
                    ('Rossi Alberto [101] Pergamino', 'rossi alberto 101 pergamino', '[101]', 'Pergamino', 1);
                """);

                // Palomares
                stmt.execute("""
                    INSERT INTO palomares (nombre, direccion, latitud, longitud, id_colombofilo, propietario) VALUES 
                    ('Palomar Alas del Sur', 'Av. Mitre 1250, Pergamino', -33.8921, -60.5732, 1, 'Don Alberto Rossi');
                """);

                // Orígenes
                stmt.execute("""
                    INSERT INTO origenes (nombre, descripcion) VALUES 
                    ('Línea Janssen', 'Estirpe belga de gran velocidad y tenacidad'),
                    ('Línea Van Loon', 'Línea holandesa ideal para medio fondo y fondo'),
                    ('Criadero La Paloma Mensajera', 'Criador local de renombre');
                """);

                // Palomas con anillo estructurado
                stmt.execute("""
                    INSERT INTO palomas (pais, institucion, anio, numero, id_propietario, id_palomar, id_origen, padre_anillo, madre_anillo, sexo, color, rol, anio_nac, mes_nac, dia_nac) VALUES 
                    ('ARG', 'FCA', 21, '100234', 1, 1, 1, NULL, NULL, 'MACHO', 'AZUL', 'REPRODUCTORA', 2021, 3, 15),
                    ('ARG', 'FCA', 21, '100235', 1, 1, 2, NULL, NULL, 'HEMBRA', 'GOTEADO', 'REPRODUCTORA', 2021, 4, 10),
                    ('ARG', 'FCA', 22, '205110', 1, 1, 1, 'ARG-FCA-21-100234', 'ARG-FCA-21-100235', 'MACHO', 'ZAINO', 'VOLADORA', 2022, 9, 20),
                    ('ARG', 'FCA', 23, '314050', 1, 1, 1, 'ARG-FCA-22-205110', 'ARG-FCA-21-100235', 'HEMBRA', 'AZUL', 'VOLADORA', 2023, 10, 5),
                    ('BELG', 'KBDB', 24, '908122', 1, 1, 1, NULL, NULL, 'MACHO', NULL, 'VOLADORA', NULL, NULL, NULL);
                """);

                // Campeonato
                stmt.execute("""
                    INSERT INTO campeonatos (nombre, temporada, id_sociedad, alcance, clase, categoria) VALUES 
                    ('Campeonato Social Anual 2026', 2026, 1, 'LOCAL', 'All', 'MIXTAS');
                """);

                // Carreras
                stmt.execute("""
                    INSERT INTO carreras (nombre, fecha_hora_suelta, ciudad, punto_suelta, latitud_suelta, longitud_suelta, clima, viento, ref_km, puntos_primera, id_web, encestes, tipo_categoria) VALUES 
                    ('Suelta Santa Fe - Pergamino', '2026-10-15T07:30:00', 'Santa Fe', 'Monumento Brigadier López', -31.6333, -60.7000, 'Despejado, buena visibilidad', 'Noreste 12 km/h (cola)', 320.5, 100.0, 'WEB-SF-2026-01', 120, 'MEDIO_FONDO'),
                    ('Suelta Córdoba - Pergamino', '2026-11-02T07:00:00', 'Córdoba', 'Peaje Carlos Paz', -31.4201, -64.1888, 'Parcialmente nublado', 'Sur 18 km/h (contra)', 485.0, 100.0, 'WEB-CBA-2026-02', 100, 'FONDO');
                """);

                // Vínculo Carrera - Campeonato
                stmt.execute("""
                    INSERT INTO campeonato_carreras (id_campeonato, id_carrera) VALUES (1, 1), (1, 2);
                """);

                // Participaciones con tiempo de vuelo y velocidades
                stmt.execute("""
                    INSERT INTO participaciones (carrera_id, paloma_id, paloma_anillo, id_colombofilo, puesto, puntos, hora_llegada, segundos_vuelo, velocidad_metros_min, velocidad_kmh, distancia_km, equipo, zonas, region, metodo_marcada, categoria_edad) VALUES 
                    (1, 3, 'ARG-FCA-22-205110', 1, 1, 100.0, '2026-10-15T11:42:30', 15150, 1269.31, 76.16, 320.5, 'Equipo A', 'Zona Norte', 'Norte', 'Electrónico Benzing', 'ADULTA'),
                    (1, 4, 'ARG-FCA-23-314050', 1, 2, 95.0, '2026-10-15T11:46:15', 15375, 1250.73, 75.04, 320.5, 'Equipo A', 'Zona Norte', 'Norte', 'Electrónico Benzing', 'PICHON');
                """);

                System.out.println("Datos iniciales enriquecidos cargados con éxito.");
            }
        } catch (SQLException e) {
            System.err.println("Aviso al cargar datos de ejemplo: " + e.getMessage());
        }
    }
}
