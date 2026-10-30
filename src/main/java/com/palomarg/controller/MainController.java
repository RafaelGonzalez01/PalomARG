package com.palomarg.controller;

import com.palomarg.database.DatabaseManager;
import com.palomarg.repository.*;
import com.palomarg.repository.sqlite.*;
import com.palomarg.service.AuthService;
import com.palomarg.service.BusquedaColombofiloService;
import com.palomarg.service.ReclamoService;

/**
 * Controlador principal del patrón MVC para coordinar los repositorios y servicios de PalomARG.
 * Cumple con la regla de exponer exclusivamente interfaces para desacoplar la interfaz gráfica de la base de datos.
 */
public class MainController {
    private final PalomaRepository palomaRepo;
    private final OrigenRepository origenRepo;
    private final PalomarRepository palomarRepo;
    private final CarreraRepository carreraRepo;
    private final CampeonatoRepository campeonatoRepo;
    private final ParticipacionCarreraRepository participacionRepo;
    private final ColombofiloRepository colombofiloRepo;
    private final UsuarioRepository usuarioRepo;
    private final SociedadRepository sociedadRepo;
    private final ZonaRepository zonaRepo;
    private final AliasColombofiloRepository aliasRepo;
    private final ReclamoPalomarRepository reclamoRepo;

    // Servicios de Fase 2
    private final AuthService authService;
    private final ReclamoService reclamoService;
    private final BusquedaColombofiloService busquedaService;

    public MainController() {
        // 1. Inicializar base de datos SQLite y datos de muestra
        DatabaseManager.inicializarBaseDeDatos();
        DatabaseManager.cargarDatosDeEjemploSiEstaVacia();

        // 2. Inicializar repositorios desacoplados
        this.usuarioRepo = new SqliteUsuarioRepository();
        this.sociedadRepo = new SqliteSociedadRepository();
        this.zonaRepo = new SqliteZonaRepository();
        this.colombofiloRepo = new SqliteColombofiloRepository(this.sociedadRepo, this.zonaRepo, this.usuarioRepo);
        this.aliasRepo = new SqliteAliasColombofiloRepository(this.colombofiloRepo);
        this.reclamoRepo = new SqliteReclamoPalomarRepository(this.usuarioRepo, this.colombofiloRepo);
        this.origenRepo = new SqliteOrigenRepository();
        this.palomarRepo = new SqlitePalomarRepository(this.colombofiloRepo);
        this.palomaRepo = new SqlitePalomaRepository(this.origenRepo, this.palomarRepo, this.colombofiloRepo);
        this.carreraRepo = new SqliteCarreraRepository();
        this.campeonatoRepo = new SqliteCampeonatoRepository(this.sociedadRepo, this.carreraRepo);
        this.participacionRepo = new SqliteParticipacionCarreraRepository(this.palomaRepo, this.carreraRepo, this.colombofiloRepo);

        // 3. Inicializar servicios de Fase 2
        this.authService = new AuthService(this.usuarioRepo);
        this.reclamoService = new ReclamoService(this.reclamoRepo, this.colombofiloRepo, this.aliasRepo, this.palomaRepo);
        this.busquedaService = new BusquedaColombofiloService(this.colombofiloRepo, this.palomaRepo);
    }

    public PalomaRepository getPalomaRepo() {
        return palomaRepo;
    }

    public OrigenRepository getOrigenRepo() {
        return origenRepo;
    }

    public PalomarRepository getPalomarRepo() {
        return palomarRepo;
    }

    public CarreraRepository getCarreraRepo() {
        return carreraRepo;
    }

    public CampeonatoRepository getCampeonatoRepo() {
        return campeonatoRepo;
    }

    public ParticipacionCarreraRepository getParticipacionRepo() {
        return participacionRepo;
    }

    public ColombofiloRepository getColombofiloRepo() {
        return colombofiloRepo;
    }

    public UsuarioRepository getUsuarioRepo() {
        return usuarioRepo;
    }

    public SociedadRepository getSociedadRepo() {
        return sociedadRepo;
    }

    public ZonaRepository getZonaRepo() {
        return zonaRepo;
    }

    public AliasColombofiloRepository getAliasRepo() {
        return aliasRepo;
    }

    public ReclamoPalomarRepository getReclamoRepo() {
        return reclamoRepo;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public ReclamoService getReclamoService() {
        return reclamoService;
    }

    public BusquedaColombofiloService getBusquedaService() {
        return busquedaService;
    }
}
