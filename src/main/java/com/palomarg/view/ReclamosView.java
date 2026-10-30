package com.palomarg.view;

import com.palomarg.model.entity.AliasColombofilo;
import com.palomarg.model.entity.Colombofilo;
import com.palomarg.model.entity.ReclamoPalomar;
import com.palomarg.model.entity.Usuario;
import com.palomarg.model.enums.EstadoReclamo;
import com.palomarg.repository.ColombofiloRepository;
import com.palomarg.service.ReclamoService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Vista de gestión de reclamos para usuarios MASTER.
 * Permite aprobar/rechazar reclamos y buscar/fusionar colombófilos.
 */
public class ReclamosView extends VBox {

    private final ReclamoService reclamoService;
    private final ColombofiloRepository colombofiloRepo;
    private final Stage ownerStage;

    private TableView<ReclamoPalomar> tablaReclamos;
    private Label lblDetalle;
    private Label lblBadgePendientes;
    private Label lblBadgeNuevos;
    private TextField busquedaGlobalField;
    private ListView<Colombofilo> listaBusquedaGlobal;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ReclamosView(ReclamoService reclamoService, ColombofiloRepository colombofiloRepo, Stage ownerStage) {
        this.reclamoService = reclamoService;
        this.colombofiloRepo = colombofiloRepo;
        this.ownerStage = ownerStage;
        construirInterfaz();
    }

    private void construirInterfaz() {
        setSpacing(0);
        setPadding(new Insets(0));

        // Encabezado
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 20, 16, 20));
        header.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1px 0;");

        Label lblTitulo = new Label("📋 Gestión de Reclamos");
        lblTitulo.getStyleClass().add("section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Badges de notificación
        lblBadgePendientes = new Label();
        lblBadgePendientes.getStyleClass().add("badge-notificacion");

        lblBadgeNuevos = new Label();
        lblBadgeNuevos.getStyleClass().add("badge-notificacion");
        lblBadgeNuevos.setStyle(lblBadgeNuevos.getStyle() + " -fx-background-color: #d97706;");

        Button btnRefrescar = new Button("🔄 Actualizar");
        btnRefrescar.getStyleClass().add("btn-secondary");
        btnRefrescar.setOnAction(e -> recargarDatos());

        header.getChildren().addAll(lblTitulo, spacer, lblBadgePendientes, lblBadgeNuevos, btnRefrescar);

        // TabPane principal
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        Tab tabPendientes = new Tab("⏳ Reclamos Pendientes", crearPanelReclamos());
        Tab tabBusqueda = new Tab("🔍 Buscar Colombófilo", crearPanelBusquedaGlobal());
        Tab tabFusion = new Tab("🔗 Fusionar", crearPanelFusion());

        tabPane.getTabs().addAll(tabPendientes, tabBusqueda, tabFusion);

        getChildren().addAll(header, tabPane);
        recargarDatos();
    }

    // -----------------------------------------------------------------------
    //  Panel de Reclamos Pendientes
    // -----------------------------------------------------------------------

    private SplitPane crearPanelReclamos() {
        // Tabla de reclamos
        tablaReclamos = new TableView<>();
        tablaReclamos.setPlaceholder(new Label("No hay reclamos pendientes"));
        tablaReclamos.getStyleClass().add("table-view");

        TableColumn<ReclamoPalomar, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setPrefWidth(140);
        colFecha.setCellValueFactory(c -> {
            ReclamoPalomar r = c.getValue();
            String fecha = r.getFecha() != null ? r.getFecha().format(FORMATTER) : "";
            return new SimpleStringProperty(fecha);
        });

        TableColumn<ReclamoPalomar, String> colUsuario = new TableColumn<>("Usuario");
        colUsuario.setPrefWidth(140);
        colUsuario.setCellValueFactory(c -> {
            Usuario u = c.getValue().getUsuario();
            return new SimpleStringProperty(u != null ? u.getNombreUsuario() : "");
        });

        TableColumn<ReclamoPalomar, String> colColombofilo = new TableColumn<>("Colombófilo Candidato");
        colColombofilo.setPrefWidth(200);
        colColombofilo.setCellValueFactory(c -> {
            Colombofilo col = c.getValue().getColombofilo();
            return new SimpleStringProperty(col != null ? col.getNombre() : "");
        });

        TableColumn<ReclamoPalomar, String> colCiudad = new TableColumn<>("Ciudad");
        colCiudad.setPrefWidth(120);
        colCiudad.setCellValueFactory(c -> {
            Colombofilo col = c.getValue().getColombofilo();
            return new SimpleStringProperty(col != null && col.getLocalidad() != null ? col.getLocalidad() : "");
        });

        TableColumn<ReclamoPalomar, String> colCodigo = new TableColumn<>("Código");
        colCodigo.setPrefWidth(80);
        colCodigo.setCellValueFactory(c -> {
            Colombofilo col = c.getValue().getColombofilo();
            return new SimpleStringProperty(col != null && col.getCodigo() != null ? col.getCodigo() : "");
        });

        TableColumn<ReclamoPalomar, String> colPalomas = new TableColumn<>("Palomas");
        colPalomas.setPrefWidth(80);
        colPalomas.setCellValueFactory(c -> {
            Colombofilo col = c.getValue().getColombofilo();
            if (col == null || col.getId() == null) return new SimpleStringProperty("0");
            // No hay acceso directo al palomaRepo aquí; mostramos "-"
            return new SimpleStringProperty("-");
        });

        tablaReclamos.getColumns().addAll(colFecha, colUsuario, colColombofilo, colCiudad, colCodigo, colPalomas);

        // Panel de detalle
        VBox panelDetalle = crearPanelDetalle();
        panelDetalle.setPrefWidth(320);

        tablaReclamos.getSelectionModel().selectedItemProperty().addListener((obs, old, reclamo) -> {
            actualizarDetalle(reclamo, panelDetalle);
        });

        SplitPane split = new SplitPane(tablaReclamos, panelDetalle);
        split.setDividerPositions(0.65);
        return split;
    }

    private VBox crearPanelDetalle() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: #ffffff;");

        lblDetalle = new Label("Seleccione un reclamo para ver el detalle.");
        lblDetalle.setWrapText(true);
        lblDetalle.setStyle("-fx-font-size: 14px; -fx-text-fill: #475569;");

        panel.getChildren().add(lblDetalle);
        return panel;
    }

    private void actualizarDetalle(ReclamoPalomar reclamo, VBox panel) {
        panel.getChildren().clear();

        if (reclamo == null) {
            Label lbl = new Label("Seleccione un reclamo para ver el detalle.");
            lbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #475569;");
            lbl.setWrapText(true);
            panel.getChildren().add(lbl);
            return;
        }

        Label lblTitle = new Label("📋 Detalle del Reclamo #" + reclamo.getId());
        lblTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        VBox infoBox = new VBox(6);
        infoBox.setStyle("-fx-background-color: #f8fafc; -fx-padding: 12px; -fx-background-radius: 8px; -fx-border-color: #e2e8f0; -fx-border-radius: 8px;");

        infoBox.getChildren().addAll(
            infoRow("👤 Usuario:", reclamo.getUsuario() != null ? reclamo.getUsuario().getNombreUsuario() : "—"),
            infoRow("📅 Fecha:", reclamo.getFecha() != null ? reclamo.getFecha().format(FORMATTER) : "—"),
            infoRow("🕊️ Colombófilo:", reclamo.getColombofilo() != null ? reclamo.getColombofilo().getNombre() : "—"),
            infoRow("📍 Ciudad:", reclamo.getColombofilo() != null && reclamo.getColombofilo().getLocalidad() != null
                ? reclamo.getColombofilo().getLocalidad() : "—"),
            infoRow("🔢 Código:", reclamo.getColombofilo() != null && reclamo.getColombofilo().getCodigo() != null
                ? reclamo.getColombofilo().getCodigo() : "—"),
            infoRow("📊 Estado:", reclamo.getEstado() != null ? reclamo.getEstado().getDescripcion() : "—")
        );

        Button btnAprobar = new Button("✅ Aprobar");
        btnAprobar.getStyleClass().add("btn-success");
        btnAprobar.setMaxWidth(Double.MAX_VALUE);
        btnAprobar.setOnAction(e -> aprobarReclamo(reclamo));

        Button btnRechazar = new Button("❌ Rechazar");
        btnRechazar.getStyleClass().add("btn-danger");
        btnRechazar.setMaxWidth(Double.MAX_VALUE);
        btnRechazar.setOnAction(e -> rechazarReclamo(reclamo));

        VBox botonesBox = new VBox(8, btnAprobar, btnRechazar);

        panel.getChildren().addAll(lblTitle, infoBox, botonesBox);
    }

    private HBox infoRow(String etiqueta, String valor) {
        Label lbl1 = new Label(etiqueta);
        lbl1.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-min-width: 110px;");
        Label lbl2 = new Label(valor);
        lbl2.setStyle("-fx-font-size: 14px; -fx-text-fill: #334155;");
        lbl2.setWrapText(true);
        HBox row = new HBox(8, lbl1, lbl2);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private void aprobarReclamo(ReclamoPalomar reclamo) {
        // Pedir nombre web (alias) - opcional
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Aprobar Reclamo");
        dialog.setHeaderText("Aprobar solicitud de: " + (reclamo.getUsuario() != null ? reclamo.getUsuario().getNombreUsuario() : ""));
        dialog.setContentText("Nombre en la web (alias, opcional):");

        Optional<String> result = dialog.showAndWait();

        AliasColombofilo alias = null;
        if (result.isPresent() && !result.get().isBlank()) {
            alias = new AliasColombofilo();
            alias.setNombreWeb(result.get().trim());
            alias.setCiudad(reclamo.getColombofilo() != null ? reclamo.getColombofilo().getLocalidad() : null);
        }

        try {
            reclamoService.aprobarReclamo(reclamo, alias);
            mostrarInfo("Reclamo Aprobado",
                "El colombófilo '" + (reclamo.getColombofilo() != null ? reclamo.getColombofilo().getNombre() : "") +
                "' fue vinculado al usuario '" + (reclamo.getUsuario() != null ? reclamo.getUsuario().getNombreUsuario() : "") + "'.");
            recargarDatos();
        } catch (Exception ex) {
            mostrarError("Error al aprobar", ex.getMessage());
        }
    }

    private void rechazarReclamo(ReclamoPalomar reclamo) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Rechazar Reclamo");
        dialog.setHeaderText("Ingrese el motivo del rechazo:");
        dialog.setContentText("Motivo:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return;

        try {
            reclamoService.rechazarReclamo(reclamo, result.get().trim());
            mostrarInfo("Reclamo Rechazado", "El reclamo fue rechazado con el motivo: " + result.get().trim());
            recargarDatos();
        } catch (Exception ex) {
            mostrarError("Error al rechazar", ex.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    //  Panel de Búsqueda Global
    // -----------------------------------------------------------------------

    private VBox crearPanelBusquedaGlobal() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(20));

        Label lblTitulo = new Label("🔍 Buscar colombófilos en el sistema");
        lblTitulo.getStyleClass().add("section-title");

        Label lblInfo = new Label("Busca cualquier colombófilo (con o sin usuario vinculado).");
        lblInfo.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");

        busquedaGlobalField = new TextField();
        busquedaGlobalField.getStyleClass().add("text-field-grande");
        busquedaGlobalField.setPromptText("Nombre, código o localidad...");

        Button btnBuscar = new Button("🔍 Buscar");
        btnBuscar.getStyleClass().add("btn-primary");
        btnBuscar.setOnAction(e -> realizarBusquedaGlobal());
        busquedaGlobalField.setOnAction(e -> realizarBusquedaGlobal());

        HBox busquedaRow = new HBox(10, busquedaGlobalField, btnBuscar);
        HBox.setHgrow(busquedaGlobalField, Priority.ALWAYS);
        busquedaRow.setAlignment(Pos.CENTER_LEFT);

        listaBusquedaGlobal = new ListView<>();
        VBox.setVgrow(listaBusquedaGlobal, Priority.ALWAYS);
        listaBusquedaGlobal.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Colombofilo item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String codigo = item.getCodigo() != null ? item.getCodigo() + " " : "";
                    String localidad = item.getLocalidad() != null ? " - " + item.getLocalidad() : "";
                    String usuario = item.getUsuario() != null ? " [👤 " + item.getUsuario().getNombreUsuario() + "]" : " [sin usuario]";
                    setText(codigo + item.getNombre() + localidad + usuario);
                }
            }
        });

        panel.getChildren().addAll(lblTitulo, lblInfo, busquedaRow, listaBusquedaGlobal);
        return panel;
    }

    private void realizarBusquedaGlobal() {
        String query = busquedaGlobalField.getText();
        if (query == null || query.isBlank()) {
            listaBusquedaGlobal.getItems().setAll(colombofiloRepo.findAll());
            return;
        }
        listaBusquedaGlobal.getItems().setAll(colombofiloRepo.search(query.trim()));
    }

    // -----------------------------------------------------------------------
    //  Panel de Fusión
    // -----------------------------------------------------------------------

    private VBox crearPanelFusion() {
        VBox panel = new VBox(16);
        panel.setPadding(new Insets(20));

        Label lblTitulo = new Label("🔗 Fusionar Colombófilos");
        lblTitulo.getStyleClass().add("section-title");

        Label lblInfo = new Label(
            "Seleccione el colombófilo ORIGEN (que se eliminará) y el DESTINO (que recibirá sus palomas y resultados).\n" +
            "⚠ Esta acción es irreversible."
        );
        lblInfo.setWrapText(true);
        lblInfo.setStyle("-fx-font-size: 14px; -fx-text-fill: #475569;");

        // ComboBox para origen y destino
        ComboBox<Colombofilo> cmbOrigen = new ComboBox<>();
        cmbOrigen.setPromptText("Seleccionar colombófilo ORIGEN");
        cmbOrigen.setMaxWidth(Double.MAX_VALUE);

        ComboBox<Colombofilo> cmbDestino = new ComboBox<>();
        cmbDestino.setPromptText("Seleccionar colombófilo DESTINO");
        cmbDestino.setMaxWidth(Double.MAX_VALUE);

        // Cargar colombófilos
        List<Colombofilo> todos = colombofiloRepo.findAll();
        cmbOrigen.getItems().setAll(todos);
        cmbDestino.getItems().setAll(todos);

        Button btnFusionar = new Button("🔗 Fusionar");
        btnFusionar.getStyleClass().add("btn-warning");
        btnFusionar.setMaxWidth(Double.MAX_VALUE);
        btnFusionar.setOnAction(e -> {
            Colombofilo origen = cmbOrigen.getValue();
            Colombofilo destino = cmbDestino.getValue();

            if (origen == null || destino == null) {
                mostrarError("Error", "Seleccione ambos colombófilos.");
                return;
            }
            if (origen.getId().equals(destino.getId())) {
                mostrarError("Error", "No puede fusionar un colombófilo consigo mismo.");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmar Fusión");
            confirm.setHeaderText("¿Está seguro de fusionar?");
            confirm.setContentText(
                "ORIGEN: " + origen.getNombre() + " → será ELIMINADO\n" +
                "DESTINO: " + destino.getNombre() + " → recibirá todas las palomas y resultados\n\n" +
                "⚠ Esta acción es IRREVERSIBLE."
            );

            confirm.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.OK) {
                    try {
                        reclamoService.fusionarColombofilos(origen, destino);
                        mostrarInfo("Fusión completada",
                            "El colombófilo '" + origen.getNombre() + "' fue fusionado en '" + destino.getNombre() + "' exitosamente.");
                        // Recargar combos
                        List<Colombofilo> actualizados = colombofiloRepo.findAll();
                        cmbOrigen.getItems().setAll(actualizados);
                        cmbDestino.getItems().setAll(actualizados);
                        cmbOrigen.setValue(null);
                        cmbDestino.setValue(null);
                    } catch (Exception ex) {
                        mostrarError("Error al fusionar", ex.getMessage());
                    }
                }
            });
        });

        panel.getChildren().addAll(lblTitulo, lblInfo,
            new Label("Colombófilo ORIGEN (se eliminará):"), cmbOrigen,
            new Label("Colombófilo DESTINO (receptor):"), cmbDestino,
            btnFusionar);

        return panel;
    }

    // -----------------------------------------------------------------------
    //  Recarga de datos
    // -----------------------------------------------------------------------

    public void recargarDatos() {
        List<ReclamoPalomar> pendientes = reclamoService.getReclamos(EstadoReclamo.PENDIENTE);
        tablaReclamos.setItems(FXCollections.observableArrayList(pendientes));

        int countPendientes = pendientes.size();
        int countSinUsuario = reclamoService.colombofilosNuevosSinUsuario().size();

        if (countPendientes > 0) {
            lblBadgePendientes.setText("⏳ " + countPendientes + " pendiente" + (countPendientes != 1 ? "s" : ""));
            lblBadgePendientes.setVisible(true);
            lblBadgePendientes.setManaged(true);
        } else {
            lblBadgePendientes.setVisible(false);
            lblBadgePendientes.setManaged(false);
        }

        if (countSinUsuario > 0) {
            lblBadgeNuevos.setText("🕊️ " + countSinUsuario + " sin usuario");
            lblBadgeNuevos.setVisible(true);
            lblBadgeNuevos.setManaged(true);
        } else {
            lblBadgeNuevos.setVisible(false);
            lblBadgeNuevos.setManaged(false);
        }
    }

    private void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
