package com.palomarg.view;

import com.palomarg.model.entity.Origen;
import com.palomarg.model.entity.Palomar;
import com.palomarg.repository.OrigenRepository;
import com.palomarg.repository.PalomarRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Vista de Datos del Palomar y Orígenes / Estirpes genéticas.
 */
public class PalomarView extends VBox {
    private final PalomarRepository palomarRepo;
    private final OrigenRepository origenRepo;

    private TextField txtNombrePalomar;
    private TextField txtPropietario;
    private TextField txtUbicacion;
    private TableView<Origen> tablaOrigenes;
    private Palomar palomarActual;

    public PalomarView(PalomarRepository palomarRepo, OrigenRepository origenRepo) {
        this.palomarRepo = palomarRepo;
        this.origenRepo = origenRepo;

        setSpacing(24);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: #f1f5f9;");

        construirInterfaz();
        cargarDatos();
    }

    @SuppressWarnings("unchecked")
    private void construirInterfaz() {
        Label lblTitulo = new Label("🏠 Mi Palomar y Estirpes de Origen");
        lblTitulo.getStyleClass().add("section-title");

        // Tarjeta Datos del Palomar
        VBox cardPalomar = new VBox(16);
        cardPalomar.getStyleClass().add("stat-card");

        Label lblSub1 = new Label("📋 Datos Principales de las Instalaciones");
        lblSub1.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a;");

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(12);

        Label l1 = new Label("Nombre del Palomar:");
        l1.getStyleClass().add("form-label");
        txtNombrePalomar = new TextField();
        txtNombrePalomar.getStyleClass().add("text-field-grande");
        txtNombrePalomar.setPrefWidth(350);

        Label l2 = new Label("Propietario / Colombófilo:");
        l2.getStyleClass().add("form-label");
        txtPropietario = new TextField();
        txtPropietario.getStyleClass().add("text-field-grande");
        txtPropietario.setPrefWidth(350);

        Label l3 = new Label("Ubicación / Coordenadas:");
        l3.getStyleClass().add("form-label");
        txtUbicacion = new TextField();
        txtUbicacion.getStyleClass().add("text-field-grande");
        txtUbicacion.setPrefWidth(350);

        grid.add(l1, 0, 0); grid.add(txtNombrePalomar, 1, 0);
        grid.add(l2, 0, 1); grid.add(txtPropietario, 1, 1);
        grid.add(l3, 0, 2); grid.add(txtUbicacion, 1, 2);

        Button btnGuardarPalomar = new Button("💾 Guardar Datos del Palomar");
        btnGuardarPalomar.getStyleClass().add("btn-success");
        btnGuardarPalomar.setOnAction(e -> guardarPalomar());

        cardPalomar.getChildren().addAll(lblSub1, grid, btnGuardarPalomar);

        // Tarjeta Orígenes / Estirpes
        VBox cardOrigenes = new VBox(16);
        cardOrigenes.getStyleClass().add("stat-card");
        VBox.setVgrow(cardOrigenes, Priority.ALWAYS);

        Label lblSub2 = new Label("🧬 Estirpes y Criaderos de Origen Registrados");
        lblSub2.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a;");

        tablaOrigenes = new TableView<>();
        tablaOrigenes.getStyleClass().add("table-view");
        tablaOrigenes.setPrefHeight(220);

        TableColumn<Origen, String> colNom = new TableColumn<>("Nombre de la Línea / Origen");
        colNom.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
        colNom.setPrefWidth(260);

        TableColumn<Origen, String> colDesc = new TableColumn<>("Descripción / Características");
        colDesc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescripcion()));
        colDesc.setPrefWidth(420);

        tablaOrigenes.getColumns().addAll(colNom, colDesc);

        // Botón nuevo origen
        HBox hboxOrigen = new HBox(12);
        hboxOrigen.setAlignment(Pos.CENTER_LEFT);

        TextField txtNuevoOrigen = new TextField();
        txtNuevoOrigen.setPromptText("Nombre de la nueva estirpe...");
        txtNuevoOrigen.getStyleClass().add("text-field-grande");
        txtNuevoOrigen.setPrefWidth(280);

        TextField txtDescOrigen = new TextField();
        txtDescOrigen.setPromptText("Descripción breve...");
        txtDescOrigen.getStyleClass().add("text-field-grande");
        txtDescOrigen.setPrefWidth(300);

        Button btnAgregarOrigen = new Button("➕ Agregar Estirpe");
        btnAgregarOrigen.getStyleClass().add("btn-primary");
        btnAgregarOrigen.setOnAction(e -> {
            String nom = txtNuevoOrigen.getText();
            if (nom != null && !nom.trim().isEmpty()) {
                origenRepo.save(new Origen(nom.trim(), txtDescOrigen.getText()));
                txtNuevoOrigen.clear();
                txtDescOrigen.clear();
                recargarOrigenes();
            }
        });

        hboxOrigen.getChildren().addAll(txtNuevoOrigen, txtDescOrigen, btnAgregarOrigen);

        cardOrigenes.getChildren().addAll(lblSub2, tablaOrigenes, hboxOrigen);

        getChildren().addAll(lblTitulo, cardPalomar, cardOrigenes);
    }

    private void cargarDatos() {
        List<Palomar> palomares = palomarRepo.findAll();
        if (!palomares.isEmpty()) {
            palomarActual = palomares.get(0);
            txtNombrePalomar.setText(palomarActual.getNombre());
            txtPropietario.setText(palomarActual.getPropietario());
            txtUbicacion.setText(palomarActual.getUbicacion());
        } else {
            palomarActual = new Palomar("Mi Palomar", "Don Colombófilo", "Argentina");
            palomarRepo.save(palomarActual);
            txtNombrePalomar.setText(palomarActual.getNombre());
            txtPropietario.setText(palomarActual.getPropietario());
            txtUbicacion.setText(palomarActual.getUbicacion());
        }
        recargarOrigenes();
    }

    private void recargarOrigenes() {
        tablaOrigenes.setItems(FXCollections.observableArrayList(origenRepo.findAll()));
    }

    private void guardarPalomar() {
        if (palomarActual == null) {
            palomarActual = new Palomar();
        }
        palomarActual.setNombre(txtNombrePalomar.getText());
        palomarActual.setPropietario(txtPropietario.getText());
        palomarActual.setUbicacion(txtUbicacion.getText());
        palomarRepo.save(palomarActual);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Guardado");
        alert.setHeaderText(null);
        alert.setContentText("Los datos del palomar fueron guardados con éxito.");
        alert.showAndWait();
    }
}
