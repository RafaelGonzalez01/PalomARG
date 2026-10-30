package com.palomarg.view;

import com.palomarg.model.entity.Colombofilo;
import com.palomarg.model.entity.Usuario;
import com.palomarg.model.enums.EstadoReclamo;
import com.palomarg.repository.*;
import com.palomarg.service.AuthService;
import com.palomarg.service.ReclamoService;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

/**
 * Vista principal con encabezado institucional, barra de navegación accesible
 * y panel central con intercambio dinámico de vistas.
 */
public class MainView extends BorderPane {
    private final PalomaRepository palomaRepo;
    private final OrigenRepository origenRepo;
    private final PalomarRepository palomarRepo;
    private final CarreraRepository carreraRepo;
    private final ParticipacionCarreraRepository partRepo;
    private final ColombofiloRepository colombofiloRepo;
    private final ReclamoPalomarRepository reclamoRepo;
    private final ReclamoService reclamoService;
    private final Usuario usuarioActual;

    private StackPane contenedorCentral;
    private DashboardView dashboardView;
    private PalomaView palomaView;
    private CarreraView carreraView;
    private PalomarView palomarView;
    private ReclamosView reclamosView;

    private final Map<String, Button> navButtons = new HashMap<>();
    private String vistaActual = "INICIO";

    private Button btnReclamos;
    private Label badgeReclamos;

    private Runnable onCerrarSesion;

    /** Constructor backward-compatible sin usuario (para tests o uso heredado). */
    public MainView(Stage stage, PalomaRepository palomaRepo, OrigenRepository origenRepo,
                    PalomarRepository palomarRepo, CarreraRepository carreraRepo,
                    ParticipacionCarreraRepository partRepo) {
        this(stage, palomaRepo, origenRepo, palomarRepo, carreraRepo, partRepo, null);
    }

    public MainView(Stage stage, PalomaRepository palomaRepo, OrigenRepository origenRepo,
                    PalomarRepository palomarRepo, CarreraRepository carreraRepo,
                    ParticipacionCarreraRepository partRepo, ColombofiloRepository colombofiloRepo) {
        this(stage, palomaRepo, origenRepo, palomarRepo, carreraRepo, partRepo, colombofiloRepo, null, null, null);
    }

    public MainView(Stage stage, PalomaRepository palomaRepo, OrigenRepository origenRepo,
                    PalomarRepository palomarRepo, CarreraRepository carreraRepo,
                    ParticipacionCarreraRepository partRepo, ColombofiloRepository colombofiloRepo,
                    Usuario usuarioActual, ReclamoPalomarRepository reclamoRepo, ReclamoService reclamoService) {
        this.palomaRepo = palomaRepo;
        this.origenRepo = origenRepo;
        this.palomarRepo = palomarRepo;
        this.carreraRepo = carreraRepo;
        this.partRepo = partRepo;
        this.colombofiloRepo = colombofiloRepo;
        this.usuarioActual = usuarioActual;
        this.reclamoRepo = reclamoRepo;
        this.reclamoService = reclamoService;

        construirInterfaz(stage);
    }

    public void setOnCerrarSesion(Runnable callback) {
        this.onCerrarSesion = callback;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    private void construirInterfaz(Stage stage) {
        // 1. Barra de Encabezado Superior
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("header-bar");

        VBox tituloBox = new VBox(2);
        Label lblTitulo = new Label("🕊️ PalomARG");
        lblTitulo.getStyleClass().add("header-title");

        Label lblSubtitulo = new Label("Sistema Integral de Gestión Colombófila");
        lblSubtitulo.getStyleClass().add("header-subtitle");

        tituloBox.getChildren().addAll(lblTitulo, lblSubtitulo);
        header.getChildren().add(tituloBox);

        // Spacer para empujar info de usuario a la derecha
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().add(spacer);

        // Info del usuario actual y botón cerrar sesión
        if (usuarioActual != null) {
            Label lblUsuarioInfo = new Label("👤 " + usuarioActual.getNombreUsuario());
            lblUsuarioInfo.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 15px; -fx-font-weight: bold;");

            Button btnCerrarSesion = new Button("Cerrar sesión");
            btnCerrarSesion.getStyleClass().add("btn-secondary");
            btnCerrarSesion.setStyle("-fx-font-size: 13px; -fx-padding: 6px 14px;");
            btnCerrarSesion.setOnAction(e -> {
                AuthService.cerrarSesion();
                if (onCerrarSesion != null) onCerrarSesion.run();
            });

            header.getChildren().addAll(lblUsuarioInfo, btnCerrarSesion);
        }

        setTop(header);

        // 2. Barra Lateral de Navegación
        VBox sidebar = new VBox(12);
        sidebar.getStyleClass().add("nav-bar");
        sidebar.setPrefWidth(240);

        Button btnInicio = crearBotonNavegacion("🏠 Inicio", "INICIO");
        Button btnPalomas = crearBotonNavegacion("🕊️ Mis Palomas", "PALOMAS");
        Button btnCarreras = crearBotonNavegacion("🏁 Carreras", "CARRERAS");
        Button btnPalomar = crearBotonNavegacion("🏠 Mi Palomar", "PALOMAR");

        sidebar.getChildren().addAll(btnInicio, btnPalomas, btnCarreras, btnPalomar);

        // Botón RECLAMOS solo para MASTER
        if (usuarioActual != null && usuarioActual.esMaster() && reclamoService != null) {
            HBox reclamosBox = new HBox(6);
            reclamosBox.setAlignment(Pos.CENTER_LEFT);

            btnReclamos = new Button("📋 Reclamos");
            btnReclamos.getStyleClass().add("nav-button");
            btnReclamos.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(btnReclamos, Priority.ALWAYS);
            btnReclamos.setOnAction(e -> mostrarVista("RECLAMOS"));
            navButtons.put("RECLAMOS", btnReclamos);

            badgeReclamos = new Label();
            badgeReclamos.getStyleClass().add("badge-notificacion");
            badgeReclamos.setVisible(false);
            badgeReclamos.setManaged(false);

            reclamosBox.getChildren().addAll(btnReclamos, badgeReclamos);
            sidebar.getChildren().add(reclamosBox);

            actualizarBadgeReclamos();
        }

        setLeft(sidebar);

        // 3. Vistas del Centro
        // Filtrar palomas por colombofilo del usuario si corresponde
        Colombofilo colombofiloUsuario = resolverColombofilo();

        dashboardView = new DashboardView(palomaRepo, carreraRepo, palomarRepo, this::mostrarVista, () -> {
            PalomaDialog dialog = new PalomaDialog(stage, null, palomaRepo, origenRepo, palomarRepo, colombofiloRepo);
            dialog.showAndWait();
            if (dialog.isGuardado()) {
                palomaView.recargarDatos();
                dashboardView.recargarDatos();
            }
        });

        palomaView = new PalomaView(palomaRepo, origenRepo, palomarRepo, colombofiloRepo);
        carreraView = new CarreraView(carreraRepo, partRepo);
        palomarView = new PalomarView(palomarRepo, origenRepo);

        if (reclamoService != null && colombofiloRepo != null) {
            reclamosView = new ReclamosView(reclamoService, colombofiloRepo, stage);
        }

        contenedorCentral = new StackPane(dashboardView);
        setCenter(contenedorCentral);

        marcarBotonActivo("INICIO");
    }

    /**
     * Resuelve el Colombofilo vinculado al usuario actual, si existe.
     */
    private Colombofilo resolverColombofilo() {
        if (usuarioActual == null || colombofiloRepo == null) return null;
        return colombofiloRepo.findAll().stream()
            .filter(c -> c.getUsuario() != null && c.getUsuario().getId().equals(usuarioActual.getId()))
            .findFirst()
            .orElse(null);
    }

    private void actualizarBadgeReclamos() {
        if (reclamoService == null || badgeReclamos == null) return;
        try {
            int count = reclamoService.getReclamos(EstadoReclamo.PENDIENTE).size();
            if (count > 0) {
                badgeReclamos.setText(String.valueOf(count));
                badgeReclamos.setVisible(true);
                badgeReclamos.setManaged(true);
            } else {
                badgeReclamos.setVisible(false);
                badgeReclamos.setManaged(false);
            }
        } catch (Exception ignored) {}
    }

    private Button crearBotonNavegacion(String texto, String claveVista) {
        Button btn = new Button(texto);
        btn.getStyleClass().add("nav-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> mostrarVista(claveVista));
        navButtons.put(claveVista, btn);
        return btn;
    }

    public void mostrarVista(String claveVista) {
        this.vistaActual = claveVista;
        contenedorCentral.getChildren().clear();

        switch (claveVista) {
            case "INICIO" -> {
                dashboardView.recargarDatos();
                contenedorCentral.getChildren().add(dashboardView);
            }
            case "PALOMAS" -> {
                palomaView.recargarDatos();
                contenedorCentral.getChildren().add(palomaView);
            }
            case "CARRERAS" -> {
                carreraView.recargarDatos();
                contenedorCentral.getChildren().add(carreraView);
            }
            case "PALOMAR" -> {
                contenedorCentral.getChildren().add(palomarView);
            }
            case "RECLAMOS" -> {
                if (reclamosView != null) {
                    reclamosView.recargarDatos();
                    actualizarBadgeReclamos();
                    contenedorCentral.getChildren().add(reclamosView);
                }
            }
        }

        marcarBotonActivo(claveVista);
    }

    private void marcarBotonActivo(String claveActiva) {
        navButtons.forEach((clave, boton) -> {
            boton.getStyleClass().removeAll("nav-button", "nav-button-active");
            if (clave.equals(claveActiva)) {
                boton.getStyleClass().add("nav-button-active");
            } else {
                boton.getStyleClass().add("nav-button");
            }
        });
    }
}
