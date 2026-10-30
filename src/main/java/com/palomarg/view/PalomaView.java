package com.palomarg.view;

import com.palomarg.model.entity.Paloma;
import com.palomarg.model.enums.RolPaloma;
import com.palomarg.model.enums.SexoPaloma;
import com.palomarg.repository.ColombofiloRepository;
import com.palomarg.repository.OrigenRepository;
import com.palomarg.repository.PalomaRepository;
import com.palomarg.repository.PalomarRepository;
import com.palomarg.repository.sqlite.SqliteColombofiloRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Vista de Gestión de Palomas adaptada para adultos mayores:
 * Botones grandes, tabla con filas altas y legibles, filtros accesibles.
 */
public class PalomaView extends VBox {
    private final PalomaRepository palomaRepo;
    private final OrigenRepository origenRepo;
    private final PalomarRepository palomarRepo;
    private final ColombofiloRepository colombofiloRepo;

    private TableView<Paloma> tabla;
    private ObservableList<Paloma> datosPalomas;
    private TextField txtBuscar;
    private ComboBox<SexoPaloma> cbFiltroSexo;
    private ComboBox<RolPaloma> cbFiltroRol;
    private Label lblContador;

    public PalomaView(PalomaRepository palomaRepo, OrigenRepository origenRepo, PalomarRepository palomarRepo) {
        this(palomaRepo, origenRepo, palomarRepo, new SqliteColombofiloRepository());
    }

    public PalomaView(PalomaRepository palomaRepo, OrigenRepository origenRepo, PalomarRepository palomarRepo, ColombofiloRepository colombofiloRepo) {
        this.palomaRepo = palomaRepo;
        this.origenRepo = origenRepo;
        this.palomarRepo = palomarRepo;
        this.colombofiloRepo = colombofiloRepo != null ? colombofiloRepo : new SqliteColombofiloRepository();

        setSpacing(16);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: #f1f5f9;");

        construirInterfaz();
        recargarDatos();
    }

    @SuppressWarnings("unchecked")
    private void construirInterfaz() {
        // 1. Título y Contador
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        Label lblTitulo = new Label("🕊️ Gestión y Censo de Palomas");
        lblTitulo.getStyleClass().add("section-title");

        lblContador = new Label();
        lblContador.setStyle("-fx-font-size: 16px; -fx-text-fill: #475569; -fx-font-weight: bold;");

        header.getChildren().addAll(lblTitulo, lblContador);

        // 2. Barra de Búsqueda y Filtros
        HBox barraFiltros = new HBox(12);
        barraFiltros.setAlignment(Pos.CENTER_LEFT);
        barraFiltros.setPadding(new Insets(8, 0, 8, 0));

        txtBuscar = new TextField();
        txtBuscar.setPromptText("🔍 Buscar por número de anillo...");
        txtBuscar.getStyleClass().add("text-field-grande");
        txtBuscar.setPrefWidth(280);
        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> filtrar());

        cbFiltroSexo = new ComboBox<>();
        cbFiltroSexo.setPromptText("Sexo (Todos)");
        cbFiltroSexo.getItems().add(null);
        cbFiltroSexo.getItems().addAll(SexoPaloma.values());
        cbFiltroSexo.getStyleClass().add("combo-box-grande");
        cbFiltroSexo.valueProperty().addListener((obs, oldVal, newVal) -> filtrar());

        cbFiltroRol = new ComboBox<>();
        cbFiltroRol.setPromptText("Rol / Función (Todos)");
        cbFiltroRol.getItems().add(null);
        cbFiltroRol.getItems().addAll(RolPaloma.values());
        cbFiltroRol.getStyleClass().add("combo-box-grande");
        cbFiltroRol.valueProperty().addListener((obs, oldVal, newVal) -> filtrar());

        Button btnLimpiarFiltros = new Button("🧹 Limpiar Filtros");
        btnLimpiarFiltros.getStyleClass().add("btn-secondary");
        btnLimpiarFiltros.setOnAction(e -> {
            txtBuscar.clear();
            cbFiltroSexo.setValue(null);
            cbFiltroRol.setValue(null);
            recargarDatos();
        });

        barraFiltros.getChildren().addAll(txtBuscar, cbFiltroSexo, cbFiltroRol, btnLimpiarFiltros);

        // 3. Tabla de Palomas
        tabla = new TableView<>();
        tabla.getStyleClass().add("table-view");
        VBox.setVgrow(tabla, Priority.ALWAYS);

        TableColumn<Paloma, String> colAnillo = new TableColumn<>("Anillo");
        colAnillo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNumeroAnillo()));
        colAnillo.setPrefWidth(160);

        TableColumn<Paloma, String> colSexo = new TableColumn<>("Sexo");
        colSexo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSexo() != null ? d.getValue().getSexo().toString() : ""));
        colSexo.setPrefWidth(120);

        TableColumn<Paloma, String> colColor = new TableColumn<>("Plumaje / Color");
        colColor.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getColor() != null ? d.getValue().getColor().getNombre() : ""));
        colColor.setPrefWidth(140);

        TableColumn<Paloma, String> colRol = new TableColumn<>("Función / Rol");
        colRol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRol() != null ? d.getValue().getRol().getDescripcion() : ""));
        colRol.setPrefWidth(180);

        TableColumn<Paloma, String> colNac = new TableColumn<>("Año / Nac.");
        colNac.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNacimiento() != null ? d.getValue().getNacimiento().toString() : ""));
        colNac.setPrefWidth(110);

        TableColumn<Paloma, String> colOrigen = new TableColumn<>("Origen / Estirpe");
        colOrigen.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getOrigenNombre()));
        colOrigen.setPrefWidth(180);

        TableColumn<Paloma, String> colPadre = new TableColumn<>("Padre");
        colPadre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPadreAnillo() != null ? d.getValue().getPadreAnillo() : "-"));
        colPadre.setPrefWidth(140);

        TableColumn<Paloma, String> colProp = new TableColumn<>("Dueño / Colombófilo");
        colProp.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPropietarioNombre()));
        colProp.setPrefWidth(180);

        TableColumn<Paloma, String> colMadre = new TableColumn<>("Madre");
        colMadre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMadreAnillo() != null ? d.getValue().getMadreAnillo() : "-"));
        colMadre.setPrefWidth(140);

        tabla.getColumns().addAll(colAnillo, colProp, colSexo, colColor, colRol, colNac, colOrigen, colPadre, colMadre);

        // 4. Botonera Inferior con Botones Grandes
        HBox barraAcciones = new HBox(16);
        barraAcciones.setAlignment(Pos.CENTER_LEFT);
        barraAcciones.setPadding(new Insets(8, 0, 0, 0));

        Button btnNuevo = new Button("➕ Registrar Paloma");
        btnNuevo.getStyleClass().add("btn-success");
        btnNuevo.setOnAction(e -> abrirDialogo(null));

        Button btnEditar = new Button("✏️ Modificar Seleccionada");
        btnEditar.getStyleClass().add("btn-primary");
        btnEditar.setOnAction(e -> {
            Paloma seleccionada = tabla.getSelectionModel().getSelectedItem();
            if (seleccionada != null) {
                abrirDialogo(seleccionada);
            } else {
                mostrarAviso("Seleccione una paloma de la lista para modificar.");
            }
        });

        Button btnArbolGenealogico = new Button("🌳 Ver Árbol Genealógico");
        btnArbolGenealogico.getStyleClass().add("btn-warning");
        btnArbolGenealogico.setOnAction(e -> {
            Paloma seleccionada = tabla.getSelectionModel().getSelectedItem();
            if (seleccionada != null) {
                mostrarArbolGenealogico(seleccionada);
            } else {
                mostrarAviso("Seleccione una paloma de la lista para ver su árbol genealógico.");
            }
        });

        Button btnEliminar = new Button("🗑️ Eliminar");
        btnEliminar.getStyleClass().add("btn-danger");
        btnEliminar.setOnAction(e -> eliminarSeleccionada());

        barraAcciones.getChildren().addAll(btnNuevo, btnEditar, btnArbolGenealogico, btnEliminar);

        getChildren().addAll(header, barraFiltros, tabla, barraAcciones);
    }

    public void recargarDatos() {
        datosPalomas = FXCollections.observableArrayList(palomaRepo.findAll());
        tabla.setItems(datosPalomas);
        actualizarContador(datosPalomas.size());
    }

    private void filtrar() {
        String texto = txtBuscar.getText();
        SexoPaloma sexo = cbFiltroSexo.getValue();
        RolPaloma rol = cbFiltroRol.getValue();

        var filtradas = palomaRepo.search(texto, sexo, rol);
        datosPalomas = FXCollections.observableArrayList(filtradas);
        tabla.setItems(datosPalomas);
        actualizarContador(filtradas.size());
    }

    private void actualizarContador(int total) {
        lblContador.setText("(Total: " + total + " " + (total == 1 ? "paloma" : "palomas") + ")");
    }

    private void abrirDialogo(Paloma paloma) {
        Stage stage = (Stage) getScene().getWindow();
        PalomaDialog dialog = new PalomaDialog(stage, paloma, palomaRepo, origenRepo, palomarRepo, colombofiloRepo);
        dialog.showAndWait();
        if (dialog.isGuardado()) {
            recargarDatos();
        }
    }

    private void eliminarSeleccionada() {
        Paloma seleccionada = tabla.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAviso("Seleccione una paloma de la lista para eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Está seguro de eliminar la paloma " + seleccionada.getNumeroAnillo() + "?");
        confirmacion.setContentText("Esta acción eliminará el registro de la paloma.");

        Optional<ButtonType> resp = confirmacion.showAndWait();
        if (resp.isPresent() && resp.get() == ButtonType.OK) {
            palomaRepo.deleteByNumeroAnillo(seleccionada.getNumeroAnillo());
            recargarDatos();
        }
    }

    private void mostrarArbolGenealogico(Paloma p) {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Árbol Genealógico - " + p.getNumeroAnillo());
        info.setHeaderText("Árbol Genealógico: " + p.getNumeroAnillo());

        StringBuilder sb = new StringBuilder();
        sb.append("🕊️ Paloma: ").append(p.getNumeroAnillo())
          .append(" | ").append(p.getSexo() != null ? p.getSexo().getDescripcion() : "")
          .append(" | ").append(p.getColor() != null ? p.getColor().getNombre() : "")
          .append(" (Nac: ").append(p.getNacimiento() != null ? p.getNacimiento().toString() : "S/F").append(")\n");
        sb.append("   Estirpe/Origen: ").append(p.getOrigenNombre()).append("\n\n");

        sb.append("├─ ♂ Padre: ");
        if (p.getPadreAnillo() != null) {
            sb.append(p.getPadreAnillo());
            palomaRepo.findByNumeroAnillo(p.getPadreAnillo()).ifPresent(padre -> {
                sb.append(" (").append(padre.getColor() != null ? padre.getColor().getNombre() : "").append(")");
                if (padre.getPadreAnillo() != null || padre.getMadreAnillo() != null) {
                    sb.append("\n│  ├─ ♂ Abuelo Paterno: ").append(padre.getPadreAnillo() != null ? padre.getPadreAnillo() : "Desconocido");
                    sb.append("\n│  └─ ♀ Abuela Paterna: ").append(padre.getMadreAnillo() != null ? padre.getMadreAnillo() : "Desconocida");
                }
            });
        } else {
            sb.append("No registrado");
        }
        sb.append("\n\n");

        sb.append("└─ ♀ Madre: ");
        if (p.getMadreAnillo() != null) {
            sb.append(p.getMadreAnillo());
            palomaRepo.findByNumeroAnillo(p.getMadreAnillo()).ifPresent(madre -> {
                sb.append(" (").append(madre.getColor() != null ? madre.getColor().getNombre() : "").append(")");
                if (madre.getPadreAnillo() != null || madre.getMadreAnillo() != null) {
                    sb.append("\n   ├─ ♂ Abuelo Materno: ").append(madre.getPadreAnillo() != null ? madre.getPadreAnillo() : "Desconocido");
                    sb.append("\n   └─ ♀ Abuela Materna: ").append(madre.getMadreAnillo() != null ? madre.getMadreAnillo() : "Desconocida");
                }
            });
        } else {
            sb.append("No registrada");
        }

        info.setContentText(sb.toString());
        info.getDialogPane().setPrefWidth(550);
        info.showAndWait();
    }

    private void mostrarAviso(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Aviso");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
