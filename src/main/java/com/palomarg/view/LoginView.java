package com.palomarg.view;

import com.palomarg.model.entity.AliasColombofilo;
import com.palomarg.model.entity.Colombofilo;
import com.palomarg.model.entity.ReclamoPalomar;
import com.palomarg.model.entity.Usuario;
import com.palomarg.model.enums.RolUsuario;
import com.palomarg.repository.ColombofiloRepository;
import com.palomarg.service.AuthService;
import com.palomarg.service.BusquedaColombofiloService;
import com.palomarg.service.ReclamoService;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Vista de Login y Registro para PalomARG.
 * Muestra pestañas de "Iniciar Sesión" y "Registrarse".
 * El registro permite vincular el usuario a un colombófilo existente (vía reclamo) o crear uno nuevo.
 */
public class LoginView extends VBox {

    private final AuthService authService;
    private final ReclamoService reclamoService;
    private final ColombofiloRepository colombofiloRepo;
    private Consumer<Usuario> onLoginExitoso;

    // Login
    private TextField loginUsuarioField;
    private PasswordField loginPasswordField;
    private Label loginErrorLabel;

    // Registro
    private TextField regUsuarioField;
    private PasswordField regPasswordField;
    private PasswordField regConfirmPasswordField;
    private RadioButton rbBuscarExistente;
    private RadioButton rbCrearNuevo;
    private TextField busquedaColombofiloField;
    private ListView<BusquedaColombofiloService.CandidatoPalomar> listaCandidatos;
    private Label regErrorLabel;
    private Label lblBusquedaInfo;

    private BusquedaColombofiloService.CandidatoPalomar candidatoSeleccionado = null;
    private Timeline debounceTimeline;

    public LoginView(AuthService authService, ReclamoService reclamoService, ColombofiloRepository colombofiloRepo) {
        this.authService = authService;
        this.reclamoService = reclamoService;
        this.colombofiloRepo = colombofiloRepo;
        construirInterfaz();
    }

    public void setOnLoginExitoso(Consumer<Usuario> callback) {
        this.onLoginExitoso = callback;
    }

    private void construirInterfaz() {
        setAlignment(Pos.CENTER);
        setPadding(new Insets(40));
        setSpacing(20);
        getStyleClass().add("root");
        setStyle("-fx-background-color: #f1f5f9;");

        // Título
        Label lblTitulo = new Label("🕊️ PalomARG");
        lblTitulo.getStyleClass().add("login-title");

        Label lblSubtitulo = new Label("Sistema Integral de Gestión Colombófila");
        lblSubtitulo.setStyle("-fx-font-size: 15px; -fx-text-fill: #475569;");

        VBox headerBox = new VBox(6, lblTitulo, lblSubtitulo);
        headerBox.setAlignment(Pos.CENTER);

        // TabPane
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getStyleClass().add("login-tab");

        Tab tabLogin = new Tab("Iniciar Sesión", crearPanelLogin());
        Tab tabRegistro = new Tab("Registrarse", crearPanelRegistro());

        tabPane.getTabs().addAll(tabLogin, tabRegistro);

        // Card contenedora
        VBox card = new VBox(20, headerBox, tabPane);
        card.getStyleClass().add("login-card");
        card.setMaxWidth(500);
        card.setPadding(new Insets(30));
        card.setAlignment(Pos.CENTER);

        getChildren().add(card);
        VBox.setVgrow(card, Priority.ALWAYS);
    }

    // -----------------------------------------------------------------------
    //  Panel de Login
    // -----------------------------------------------------------------------

    private VBox crearPanelLogin() {
        VBox panel = new VBox(14);
        panel.setPadding(new Insets(20, 10, 10, 10));

        Label lblUsuario = new Label("Usuario:");
        lblUsuario.getStyleClass().add("form-label");
        loginUsuarioField = new TextField();
        loginUsuarioField.getStyleClass().add("text-field-grande");
        loginUsuarioField.setPromptText("Nombre de usuario");

        Label lblPassword = new Label("Contraseña:");
        lblPassword.getStyleClass().add("form-label");
        loginPasswordField = new PasswordField();
        loginPasswordField.getStyleClass().add("text-field-grande");
        loginPasswordField.setPromptText("Contraseña");

        loginErrorLabel = new Label();
        loginErrorLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 14px;");
        loginErrorLabel.setWrapText(true);

        Button btnEntrar = new Button("🔑 Entrar");
        btnEntrar.getStyleClass().add("btn-primary");
        btnEntrar.setMaxWidth(Double.MAX_VALUE);
        btnEntrar.setOnAction(e -> realizarLogin());

        // Permitir Enter en los campos
        loginUsuarioField.setOnAction(e -> realizarLogin());
        loginPasswordField.setOnAction(e -> realizarLogin());

        panel.getChildren().addAll(
            lblUsuario, loginUsuarioField,
            lblPassword, loginPasswordField,
            loginErrorLabel,
            btnEntrar
        );
        return panel;
    }

    private void realizarLogin() {
        loginErrorLabel.setText("");
        String usuario = loginUsuarioField.getText();
        String password = loginPasswordField.getText();

        if (usuario == null || usuario.isBlank()) {
            loginErrorLabel.setText("⚠ Ingrese su nombre de usuario.");
            return;
        }
        if (password == null || password.isEmpty()) {
            loginErrorLabel.setText("⚠ Ingrese su contraseña.");
            return;
        }

        Optional<Usuario> optUsuario = authService.login(usuario, password);
        if (optUsuario.isPresent()) {
            AuthService.setUsuarioActual(optUsuario.get());
            if (onLoginExitoso != null) {
                onLoginExitoso.accept(optUsuario.get());
            }
        } else {
            loginErrorLabel.setText("❌ Usuario o contraseña incorrectos.");
        }
    }

    // -----------------------------------------------------------------------
    //  Panel de Registro
    // -----------------------------------------------------------------------

    private ScrollPane crearPanelRegistro() {
        VBox panel = new VBox(14);
        panel.setPadding(new Insets(20, 10, 10, 10));

        Label lblUsuario = new Label("Usuario:");
        lblUsuario.getStyleClass().add("form-label");
        regUsuarioField = new TextField();
        regUsuarioField.getStyleClass().add("text-field-grande");
        regUsuarioField.setPromptText("Elija un nombre de usuario único");

        Label lblPassword = new Label("Contraseña:");
        lblPassword.getStyleClass().add("form-label");
        regPasswordField = new PasswordField();
        regPasswordField.getStyleClass().add("text-field-grande");
        regPasswordField.setPromptText("Mínimo 4 caracteres");

        Label lblConfirm = new Label("Confirmar Contraseña:");
        lblConfirm.getStyleClass().add("form-label");
        regConfirmPasswordField = new PasswordField();
        regConfirmPasswordField.getStyleClass().add("text-field-grande");
        regConfirmPasswordField.setPromptText("Repita la contraseña");

        // Radio buttons para elegir tipo de palomar
        Label lblTipoPalomar = new Label("¿Tiene palomar en el sistema?");
        lblTipoPalomar.getStyleClass().add("form-label");

        ToggleGroup grupoTipo = new ToggleGroup();
        rbBuscarExistente = new RadioButton("Buscar mi palomar existente");
        rbBuscarExistente.setToggleGroup(grupoTipo);
        rbBuscarExistente.setSelected(true);
        rbBuscarExistente.setStyle("-fx-font-size: 15px; -fx-text-fill: #1e293b;");

        rbCrearNuevo = new RadioButton("Crear palomar nuevo");
        rbCrearNuevo.setToggleGroup(grupoTipo);
        rbCrearNuevo.setStyle("-fx-font-size: 15px; -fx-text-fill: #1e293b;");

        VBox radioBox = new VBox(8, rbBuscarExistente, rbCrearNuevo);

        // Panel de búsqueda de colombófilo existente
        VBox panelBusqueda = crearPanelBusqueda();
        panelBusqueda.setManaged(true);
        panelBusqueda.setVisible(true);

        // Mostrar/ocultar panel de búsqueda según selección
        rbBuscarExistente.selectedProperty().addListener((obs, old, val) -> {
            panelBusqueda.setVisible(val);
            panelBusqueda.setManaged(val);
            if (val) {
                candidatoSeleccionado = null;
                listaCandidatos.getItems().clear();
            }
        });

        regErrorLabel = new Label();
        regErrorLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 14px;");
        regErrorLabel.setWrapText(true);

        Button btnRegistrar = new Button("✅ Crear cuenta");
        btnRegistrar.getStyleClass().add("btn-primary");
        btnRegistrar.setMaxWidth(Double.MAX_VALUE);
        btnRegistrar.setOnAction(e -> realizarRegistro());

        panel.getChildren().addAll(
            lblUsuario, regUsuarioField,
            lblPassword, regPasswordField,
            lblConfirm, regConfirmPasswordField,
            lblTipoPalomar, radioBox,
            panelBusqueda,
            regErrorLabel,
            btnRegistrar
        );

        ScrollPane scroll = new ScrollPane(panel);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        return scroll;
    }

    private VBox crearPanelBusqueda() {
        VBox panel = new VBox(8);
        panel.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 12px;");

        Label lblBuscar = new Label("Buscar colombófilo:");
        lblBuscar.getStyleClass().add("form-label");

        busquedaColombofiloField = new TextField();
        busquedaColombofiloField.getStyleClass().add("text-field-grande");
        busquedaColombofiloField.setPromptText("Ej: García 101 Rosario");

        lblBusquedaInfo = new Label("Ingrese al menos 3 caracteres para buscar.");
        lblBusquedaInfo.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        listaCandidatos = new ListView<>();
        listaCandidatos.setPrefHeight(160);
        listaCandidatos.setVisible(false);
        listaCandidatos.setManaged(false);
        listaCandidatos.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(BusquedaColombofiloService.CandidatoPalomar item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    setText(item.formatoListado());
                    getStyleClass().add("candidato-item");
                }
            }
        });

        Label lblSeleccionado = new Label();
        lblSeleccionado.setStyle("-fx-font-size: 14px; -fx-text-fill: #16a34a; -fx-font-weight: bold;");
        lblSeleccionado.setWrapText(true);

        // Evento de selección
        listaCandidatos.getSelectionModel().selectedItemProperty().addListener((obs, old, candidato) -> {
            if (candidato != null) {
                candidatoSeleccionado = candidato;
                lblSeleccionado.setText("✅ Seleccionado: " + candidato.colombofilo().getNombre());
            } else {
                candidatoSeleccionado = null;
                lblSeleccionado.setText("");
            }
        });

        // Debounce en la búsqueda (400ms)
        busquedaColombofiloField.textProperty().addListener((obs, old, texto) -> {
            candidatoSeleccionado = null;
            lblSeleccionado.setText("");
            if (debounceTimeline != null) debounceTimeline.stop();

            if (texto == null || texto.trim().length() < 3) {
                listaCandidatos.setVisible(false);
                listaCandidatos.setManaged(false);
                lblBusquedaInfo.setText("Ingrese al menos 3 caracteres para buscar.");
                return;
            }

            debounceTimeline = new Timeline(new KeyFrame(Duration.millis(400), e -> {
                lblBusquedaInfo.setText("Buscando...");
                new Thread(() -> {
                    List<BusquedaColombofiloService.CandidatoPalomar> resultados =
                        reclamoService.buscarCandidatos(texto.trim());
                    Platform.runLater(() -> {
                        listaCandidatos.getItems().setAll(resultados);
                        if (resultados.isEmpty()) {
                            lblBusquedaInfo.setText("No se encontraron colombófilos similares.");
                            listaCandidatos.setVisible(false);
                            listaCandidatos.setManaged(false);
                        } else {
                            lblBusquedaInfo.setText(resultados.size() + " candidato(s) encontrado(s):");
                            listaCandidatos.setVisible(true);
                            listaCandidatos.setManaged(true);
                        }
                    });
                }).start();
            }));
            debounceTimeline.play();
        });

        panel.getChildren().addAll(lblBuscar, busquedaColombofiloField, lblBusquedaInfo, listaCandidatos, lblSeleccionado);
        return panel;
    }

    private void realizarRegistro() {
        regErrorLabel.setText("");

        String usuario = regUsuarioField.getText();
        String password = regPasswordField.getText();
        String confirm = regConfirmPasswordField.getText();

        if (usuario == null || usuario.isBlank()) {
            regErrorLabel.setText("⚠ Ingrese un nombre de usuario.");
            return;
        }
        if (password == null || password.length() < 4) {
            regErrorLabel.setText("⚠ La contraseña debe tener al menos 4 caracteres.");
            return;
        }
        if (!password.equals(confirm)) {
            regErrorLabel.setText("⚠ Las contraseñas no coinciden.");
            return;
        }

        if (rbBuscarExistente.isSelected() && candidatoSeleccionado == null) {
            regErrorLabel.setText("⚠ Seleccione su colombófilo de la lista, o elija 'Crear palomar nuevo'.");
            return;
        }

        // Confirmar antes de crear reclamo
        if (rbBuscarExistente.isSelected() && candidatoSeleccionado != null) {
            Alert confirm2 = new Alert(Alert.AlertType.CONFIRMATION);
            confirm2.setTitle("Confirmar vinculación");
            confirm2.setHeaderText("¿Es este su colombófilo?");
            confirm2.setContentText(candidatoSeleccionado.formatoListado() +
                "\n\nSe enviará un reclamo al administrador MASTER para su aprobación.");
            Optional<ButtonType> result = confirm2.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) return;
        }

        try {
            Usuario nuevoUsuario = authService.registrar(usuario.trim(), password, RolUsuario.USUARIO);

            if (rbBuscarExistente.isSelected() && candidatoSeleccionado != null) {
                // Crear reclamo de vinculación
                Colombofilo col = candidatoSeleccionado.colombofilo();
                reclamoService.crearReclamo(nuevoUsuario, col);
                mostrarInfo("Cuenta creada",
                    "Su cuenta fue creada exitosamente.\n\n" +
                    "Se envió una solicitud de vinculación al administrador.\n" +
                    "Podrá acceder a su palomar una vez que el MASTER apruebe el reclamo.");
            } else if (rbCrearNuevo.isSelected()) {
                // Crear un colombófilo nuevo y vincularlo automáticamente
                Colombofilo nuevo = new Colombofilo();
                nuevo.setNombre(usuario.trim());
                nuevo.setUsuario(nuevoUsuario);
                colombofiloRepo.save(nuevo);
                mostrarInfo("Cuenta creada", "Su cuenta y palomar fueron creados exitosamente.\nYa puede iniciar sesión.");
            }

            AuthService.setUsuarioActual(nuevoUsuario);
            if (onLoginExitoso != null) {
                onLoginExitoso.accept(nuevoUsuario);
            }

        } catch (IllegalArgumentException ex) {
            regErrorLabel.setText("❌ " + ex.getMessage());
        }
    }

    private void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
