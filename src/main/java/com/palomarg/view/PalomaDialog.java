package com.palomarg.view;

import com.palomarg.model.entity.Colombofilo;
import com.palomarg.model.entity.Origen;
import com.palomarg.model.entity.Paloma;
import com.palomarg.model.entity.Palomar;
import com.palomarg.model.enums.Colores;
import com.palomarg.model.enums.RolPaloma;
import com.palomarg.model.enums.SexoPaloma;
import com.palomarg.model.vo.FechaNac;
import com.palomarg.repository.ColombofiloRepository;
import com.palomarg.repository.OrigenRepository;
import com.palomarg.repository.PalomaRepository;
import com.palomarg.repository.PalomarRepository;
import com.palomarg.repository.sqlite.SqliteColombofiloRepository;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

/**
 * Diálogo para registrar o modificar una Paloma.
 * Diseñado con tipografía grande, etiquetas claras y controles accesibles.
 */
public class PalomaDialog extends Stage {
    private final PalomaRepository palomaRepo;
    private final OrigenRepository origenRepo;
    private final PalomarRepository palomarRepo;
    private final ColombofiloRepository colombofiloRepo;

    private TextField txtAnillo;
    private ComboBox<Colombofilo> cbPropietario;
    private ComboBox<SexoPaloma> cbSexo;
    private ComboBox<Colores> cbColor;
    private ComboBox<RolPaloma> cbRol;
    private TextField txtAnioNac;
    private ComboBox<Origen> cbOrigen;
    private ComboBox<Palomar> cbPalomar;
    private ComboBox<Paloma> cbPadre;
    private ComboBox<Paloma> cbMadre;

    private Paloma palomaEditando;
    private boolean guardado = false;

    public PalomaDialog(Stage parentStage, Paloma palomaAEditar, PalomaRepository palomaRepo, OrigenRepository origenRepo, PalomarRepository palomarRepo) {
        this(parentStage, palomaAEditar, palomaRepo, origenRepo, palomarRepo, new SqliteColombofiloRepository());
    }

    public PalomaDialog(Stage parentStage, Paloma palomaAEditar, PalomaRepository palomaRepo, OrigenRepository origenRepo, PalomarRepository palomarRepo, ColombofiloRepository colombofiloRepo) {
        this.palomaRepo = palomaRepo;
        this.origenRepo = origenRepo;
        this.palomarRepo = palomarRepo;
        this.colombofiloRepo = colombofiloRepo != null ? colombofiloRepo : new SqliteColombofiloRepository();
        this.palomaEditando = palomaAEditar;

        initModality(Modality.WINDOW_MODAL);
        initOwner(parentStage);
        setTitle(palomaAEditar == null ? "Registrar Nueva Paloma" : "Modificar Paloma: " + palomaAEditar.getNumeroAnillo());
        setResizable(false);

        construirInterfaz();
        cargarDatos();
    }

    private void construirInterfaz() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #f8fafc;");

        Label lblTitulo = new Label(palomaEditando == null ? "🕊️ Nueva Paloma" : "✏️ Modificar Paloma");
        lblTitulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a;");

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(12);

        // Número de Anillo
        Label lblAnillo = new Label("Número de Anillo (*):");
        lblAnillo.getStyleClass().add("form-label");
        txtAnillo = new TextField();
        txtAnillo.setPromptText("Ej: ARG-FCA-24-123456");
        txtAnillo.getStyleClass().add("text-field-grande");
        txtAnillo.setPrefWidth(300);
        if (palomaEditando != null) {
            txtAnillo.setDisable(true);
        }

        // Propietario (Obligatorio)
        Label lblProp = new Label("Colombófilo / Dueño (*):");
        lblProp.getStyleClass().add("form-label");
        cbPropietario = new ComboBox<>();
        cbPropietario.getStyleClass().add("combo-box-grande");
        cbPropietario.setPrefWidth(300);
        cbPropietario.getItems().addAll(colombofiloRepo.findAll());

        // Sexo (Opcional)
        Label lblSexo = new Label("Sexo (Opcional):");
        lblSexo.getStyleClass().add("form-label");
        cbSexo = new ComboBox<>();
        cbSexo.getItems().add(null);
        cbSexo.getItems().addAll(SexoPaloma.values());
        cbSexo.getStyleClass().add("combo-box-grande");
        cbSexo.setPrefWidth(300);

        // Color (Opcional)
        Label lblColor = new Label("Color / Plumaje (Opcional):");
        lblColor.getStyleClass().add("form-label");
        cbColor = new ComboBox<>();
        cbColor.getItems().add(null);
        cbColor.getItems().addAll(Colores.values());
        cbColor.getStyleClass().add("combo-box-grande");
        cbColor.setPrefWidth(300);

        // Rol
        Label lblRol = new Label("Función / Rol:");
        lblRol.getStyleClass().add("form-label");
        cbRol = new ComboBox<>();
        cbRol.getItems().addAll(RolPaloma.values());
        cbRol.getStyleClass().add("combo-box-grande");
        cbRol.setPrefWidth(300);

        // Año Nacimiento (Opcional)
        Label lblAnio = new Label("Año Nac. (Opcional):");
        lblAnio.getStyleClass().add("form-label");
        txtAnioNac = new TextField();
        txtAnioNac.setPromptText("Ej: 2024");
        txtAnioNac.getStyleClass().add("text-field-grande");
        txtAnioNac.setPrefWidth(300);

        // Origen / Línea
        Label lblOrigen = new Label("Origen / Estirpe:");
        lblOrigen.getStyleClass().add("form-label");
        cbOrigen = new ComboBox<>();
        cbOrigen.getStyleClass().add("combo-box-grande");
        cbOrigen.setPrefWidth(300);
        cbOrigen.getItems().add(null);
        cbOrigen.getItems().addAll(origenRepo.findAll());

        // Palomar
        Label lblPalomar = new Label("Palomar:");
        lblPalomar.getStyleClass().add("form-label");
        cbPalomar = new ComboBox<>();
        cbPalomar.getStyleClass().add("combo-box-grande");
        cbPalomar.setPrefWidth(300);
        cbPalomar.getItems().add(null);
        cbPalomar.getItems().addAll(palomarRepo.findAll());

        // Genealogía: Padre y Madre
        Label lblPadre = new Label("Padre:");
        lblPadre.getStyleClass().add("form-label");
        cbPadre = new ComboBox<>();
        cbPadre.getStyleClass().add("combo-box-grande");
        cbPadre.setPrefWidth(300);

        Label lblMadre = new Label("Madre:");
        lblMadre.getStyleClass().add("form-label");
        cbMadre = new ComboBox<>();
        cbMadre.getStyleClass().add("combo-box-grande");
        cbMadre.setPrefWidth(300);

        List<Paloma> todas = palomaRepo.findAll();
        cbPadre.getItems().add(null);
        cbPadre.getItems().addAll(todas);
        cbMadre.getItems().add(null);
        cbMadre.getItems().addAll(todas);

        // Posicionar en el GridPane
        grid.add(lblAnillo, 0, 0);       grid.add(txtAnillo, 1, 0);
        grid.add(lblProp, 0, 1);         grid.add(cbPropietario, 1, 1);
        grid.add(lblSexo, 0, 2);         grid.add(cbSexo, 1, 2);
        grid.add(lblColor, 0, 3);        grid.add(cbColor, 1, 3);
        grid.add(lblRol, 0, 4);          grid.add(cbRol, 1, 4);
        grid.add(lblAnio, 0, 5);         grid.add(txtAnioNac, 1, 5);
        grid.add(lblOrigen, 0, 6);       grid.add(cbOrigen, 1, 6);
        grid.add(lblPalomar, 0, 7);      grid.add(cbPalomar, 1, 7);
        grid.add(lblPadre, 0, 8);        grid.add(cbPadre, 1, 8);
        grid.add(lblMadre, 0, 9);        grid.add(cbMadre, 1, 9);

        // Botones de acción
        Button btnGuardar = new Button("💾 Guardar Paloma");
        btnGuardar.getStyleClass().add("btn-success");
        btnGuardar.setOnAction(e -> guardar());

        Button btnCancelar = new Button("❌ Cancelar");
        btnCancelar.getStyleClass().add("btn-secondary");
        btnCancelar.setOnAction(e -> close());

        HBox hboxBotones = new HBox(16, btnGuardar, btnCancelar);
        hboxBotones.setAlignment(Pos.CENTER_RIGHT);
        hboxBotones.setPadding(new Insets(10, 0, 0, 0));

        root.getChildren().addAll(lblTitulo, grid, hboxBotones);

        Scene scene = new Scene(root, 600, 680);
        try {
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        } catch (Exception ignored) {}
        setScene(scene);
    }

    private void cargarDatos() {
        if (palomaEditando != null) {
            txtAnillo.setText(palomaEditando.getNumeroAnillo());
            cbPropietario.setValue(palomaEditando.getPropietario());
            cbSexo.setValue(palomaEditando.getSexo());
            cbColor.setValue(palomaEditando.getColor());
            cbRol.setValue(palomaEditando.getRol());
            if (palomaEditando.getNacimiento() != null && palomaEditando.getNacimiento().getAnio() != null) {
                txtAnioNac.setText(String.valueOf(palomaEditando.getNacimiento().getAnio()));
            }
            cbOrigen.setValue(palomaEditando.getOrigen());
            cbPalomar.setValue(palomaEditando.getPalomar());

            if (palomaEditando.getPadre() != null) {
                cbPadre.getItems().stream()
                    .filter(p -> p != null && p.getNumeroAnillo().equalsIgnoreCase(palomaEditando.getPadre().getNumeroAnillo()))
                    .findFirst().ifPresent(cbPadre::setValue);
            }
            if (palomaEditando.getMadre() != null) {
                cbMadre.getItems().stream()
                    .filter(p -> p != null && p.getNumeroAnillo().equalsIgnoreCase(palomaEditando.getMadre().getNumeroAnillo()))
                    .findFirst().ifPresent(cbMadre::setValue);
            }
        } else {
            cbRol.setValue(RolPaloma.VOLADORA);
            txtAnioNac.setText(String.valueOf(LocalDate.now().getYear()));
            if (!cbPropietario.getItems().isEmpty()) {
                cbPropietario.setValue(cbPropietario.getItems().get(0));
            }
            if (!cbPalomar.getItems().isEmpty()) {
                cbPalomar.setValue(cbPalomar.getItems().get(0));
            }
        }
    }

    private void guardar() {
        String anillo = txtAnillo.getText();
        if (anillo == null || anillo.trim().isEmpty()) {
            mostrarAlerta("El número de anillo es obligatorio.");
            return;
        }

        if (cbPropietario.getValue() == null) {
            mostrarAlerta("Debe asignar un colombófilo propietario.");
            return;
        }

        Paloma p = palomaEditando != null ? palomaEditando : new Paloma();
        p.setNumeroAnillo(anillo.trim().toUpperCase());
        p.setPropietario(cbPropietario.getValue());
        p.setSexo(cbSexo.getValue());
        p.setColor(cbColor.getValue());
        p.setRol(cbRol.getValue() != null ? cbRol.getValue() : RolPaloma.VOLADORA);

        String anioStr = txtAnioNac.getText();
        if (anioStr != null && !anioStr.trim().isEmpty()) {
            try {
                int anio = Integer.parseInt(anioStr.trim());
                p.setNacimiento(new FechaNac(anio));
            } catch (NumberFormatException e) {
                mostrarAlerta("El año de nacimiento debe ser un número válido (ej: 2024).");
                return;
            }
        } else {
            p.setNacimiento(null);
        }

        p.setOrigen(cbOrigen.getValue());
        p.setPalomar(cbPalomar.getValue());
        p.setPadre(cbPadre.getValue());
        p.setMadre(cbMadre.getValue());

        palomaRepo.save(p);
        guardado = true;
        close();
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atención");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public boolean isGuardado() {
        return guardado;
    }
}
