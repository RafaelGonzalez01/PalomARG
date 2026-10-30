package com.palomarg.view;

import com.palomarg.model.entity.Carrera;
import com.palomarg.model.entity.ParticipacionCarrera;
import com.palomarg.repository.CarreraRepository;
import com.palomarg.repository.ParticipacionCarreraRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

/**
 * Vista de Carreras y Concursos adaptada para adultos mayores:
 * Información detallada de sueltas, distancias, condiciones y clasificaciones en vivo.
 */
public class CarreraView extends VBox {
    private final CarreraRepository carreraRepo;
    private final ParticipacionCarreraRepository partRepo;

    private TableView<Carrera> tablaCarreras;
    private Label lblContador;

    public CarreraView(CarreraRepository carreraRepo, ParticipacionCarreraRepository partRepo) {
        this.carreraRepo = carreraRepo;
        this.partRepo = partRepo;

        setSpacing(16);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: #f1f5f9;");

        construirInterfaz();
        recargarDatos();
    }

    @SuppressWarnings("unchecked")
    private void construirInterfaz() {
        // Título
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        Label lblTitulo = new Label("🏁 Carreras y Concursos");
        lblTitulo.getStyleClass().add("section-title");

        lblContador = new Label();
        lblContador.setStyle("-fx-font-size: 16px; -fx-text-fill: #475569; -fx-font-weight: bold;");

        header.getChildren().addAll(lblTitulo, lblContador);

        // Tabla de Carreras
        tablaCarreras = new TableView<>();
        tablaCarreras.getStyleClass().add("table-view");
        VBox.setVgrow(tablaCarreras, Priority.ALWAYS);

        TableColumn<Carrera, String> colNombre = new TableColumn<>("Nombre de la Suelta / Carrera");
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
        colNombre.setPrefWidth(260);

        TableColumn<Carrera, String> colFecha = new TableColumn<>("Fecha y Hora de Suelta");
        colFecha.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFechaHoraSueltaFormateada()));
        colFecha.setPrefWidth(180);

        TableColumn<Carrera, String> colDist = new TableColumn<>("Distancia (km)");
        colDist.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDistanciaKm() != null ? d.getValue().getDistanciaKm() + " km" : "-"));
        colDist.setPrefWidth(140);

        TableColumn<Carrera, String> colCat = new TableColumn<>("Categoría");
        colCat.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTipoCategoria() != null ? d.getValue().getTipoCategoria().getDescripcion() : "-"));
        colCat.setPrefWidth(200);

        TableColumn<Carrera, String> colClima = new TableColumn<>("Clima");
        colClima.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getClima() != null ? d.getValue().getClima() : "-"));
        colClima.setPrefWidth(180);

        TableColumn<Carrera, String> colViento = new TableColumn<>("Viento");
        colViento.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getViento() != null ? d.getValue().getViento() : "-"));
        colViento.setPrefWidth(180);

        tablaCarreras.getColumns().addAll(colNombre, colFecha, colDist, colCat, colClima, colViento);

        // Barra inferior de botones
        HBox barraAcciones = new HBox(16);
        barraAcciones.setAlignment(Pos.CENTER_LEFT);
        barraAcciones.setPadding(new Insets(8, 0, 0, 0));

        Button btnVerResultados = new Button("📊 Ver Clasificación y Velocidades");
        btnVerResultados.getStyleClass().add("btn-primary");
        btnVerResultados.setOnAction(e -> {
            Carrera c = tablaCarreras.getSelectionModel().getSelectedItem();
            if (c != null) {
                mostrarClasificacion(c);
            } else {
                mostrarAviso("Seleccione una carrera para ver sus resultados y velocidades.");
            }
        });

        Button btnRecargar = new Button("🔄 Actualizar Lista");
        btnRecargar.getStyleClass().add("btn-secondary");
        btnRecargar.setOnAction(e -> recargarDatos());

        barraAcciones.getChildren().addAll(btnVerResultados, btnRecargar);

        getChildren().addAll(header, tablaCarreras, barraAcciones);
    }

    public void recargarDatos() {
        List<Carrera> lista = carreraRepo.findAll();
        tablaCarreras.setItems(FXCollections.observableArrayList(lista));
        lblContador.setText("(Total: " + lista.size() + " " + (lista.size() == 1 ? "carrera" : "carreras") + ")");
    }

    @SuppressWarnings("unchecked")
    private void mostrarClasificacion(Carrera carrera) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Clasificación Oficial: " + carrera.getNombre());

        VBox root = new VBox(16);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f8fafc;");

        Label titulo = new Label("🏆 Resultados - " + carrera.getNombre());
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a;");

        Label subtitulo = new Label("Suelta: " + carrera.getFechaHoraSueltaFormateada() + " | Distancia: " + carrera.getDistanciaKm() + " km");
        subtitulo.setStyle("-fx-font-size: 15px; -fx-text-fill: #475569;");

        TableView<ParticipacionCarrera> tablaRes = new TableView<>();
        tablaRes.getStyleClass().add("table-view");
        VBox.setVgrow(tablaRes, Priority.ALWAYS);

        TableColumn<ParticipacionCarrera, String> colPuesto = new TableColumn<>("Puesto");
        colPuesto.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPuesto() != null ? "# " + d.getValue().getPuesto() : "-"));
        colPuesto.setPrefWidth(90);

        TableColumn<ParticipacionCarrera, String> colPaloma = new TableColumn<>("Paloma (Anillo)");
        colPaloma.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPalomaAnillo()));
        colPaloma.setPrefWidth(160);

        TableColumn<ParticipacionCarrera, String> colHora = new TableColumn<>("Hora de Comprobación");
        colHora.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getHoraLlegadaFormateada()));
        colHora.setPrefWidth(180);

        TableColumn<ParticipacionCarrera, String> colVel = new TableColumn<>("Velocidad (m/m)");
        colVel.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getVelocidadMpm() != null ? String.format("%.2f m/m", d.getValue().getVelocidadMpm()) : "En vuelo"));
        colVel.setPrefWidth(150);

        TableColumn<ParticipacionCarrera, String> colPts = new TableColumn<>("Puntos");
        colPts.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPuntos() != null ? String.format("%.1f pts", d.getValue().getPuntos()) : "-"));
        colPts.setPrefWidth(110);

        tablaRes.getColumns().addAll(colPuesto, colPaloma, colHora, colVel, colPts);

        List<ParticipacionCarrera> participaciones = partRepo.findByCarreraId(carrera.getId());
        tablaRes.setItems(FXCollections.observableArrayList(participaciones));

        Button btnCerrar = new Button("Cerrar");
        btnCerrar.getStyleClass().add("btn-secondary");
        btnCerrar.setOnAction(e -> stage.close());

        root.getChildren().addAll(titulo, subtitulo, tablaRes, btnCerrar);

        Scene scene = new Scene(root, 750, 480);
        try {
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        } catch (Exception ignored) {}
        stage.setScene(scene);
        stage.showAndWait();
    }

    private void mostrarAviso(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Aviso");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
