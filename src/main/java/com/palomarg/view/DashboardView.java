package com.palomarg.view;

import com.palomarg.model.entity.Palomar;
import com.palomarg.model.enums.RolPaloma;
import com.palomarg.repository.CarreraRepository;
import com.palomarg.repository.PalomaRepository;
import com.palomarg.repository.PalomarRepository;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Menú principal / Tablero de bienvenida especialmente diseñado para adultos mayores:
 * Tarjetas numéricas gigantes, botones grandes con iconos y textos de alto contraste.
 */
public class DashboardView extends VBox {
    private final PalomaRepository palomaRepo;
    private final CarreraRepository carreraRepo;
    private final PalomarRepository palomarRepo;
    private final Consumer<String> navegador;
    private final Runnable accionNuevaPaloma;

    private Label lblTotalPalomas;
    private Label lblVoladoras;
    private Label lblReproductoras;
    private Label lblCarreras;
    private Label lblBienvenidaPalomar;

    public DashboardView(PalomaRepository palomaRepo, CarreraRepository carreraRepo, PalomarRepository palomarRepo,
                         Consumer<String> navegador, Runnable accionNuevaPaloma) {
        this.palomaRepo = palomaRepo;
        this.carreraRepo = carreraRepo;
        this.palomarRepo = palomarRepo;
        this.navegador = navegador;
        this.accionNuevaPaloma = accionNuevaPaloma;

        setSpacing(24);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: #f1f5f9;");

        construirInterfaz();
        recargarDatos();
    }

    private void construirInterfaz() {
        // 1. Banner de Saludo
        VBox banner = new VBox(6);
        banner.getStyleClass().add("stat-card");
        banner.setStyle("-fx-background-color: #ffffff; -fx-border-color: #bfdbfe; -fx-border-width: 2px;");

        String fechaHoy = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy", Locale.of("es", "ES")));
        // Capitalizar la primera letra
        fechaHoy = fechaHoy.substring(0, 1).toUpperCase() + fechaHoy.substring(1);

        Label lblFecha = new Label("📅 " + fechaHoy);
        lblFecha.setStyle("-fx-font-size: 15px; -fx-text-fill: #64748b; -fx-font-weight: bold;");

        lblBienvenidaPalomar = new Label("👋 Bienvenido a PalomARG - Gestión Colombófila");
        lblBienvenidaPalomar.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a;");

        Label lblSub = new Label("Seleccione una opción con los botones grandes para administrar su palomar con facilidad.");
        lblSub.setStyle("-fx-font-size: 16px; -fx-text-fill: #334155;");

        banner.getChildren().addAll(lblFecha, lblBienvenidaPalomar, lblSub);

        // 2. Tarjetas de Estadísticas Grandes
        HBox hboxStats = new HBox(20);
        hboxStats.setAlignment(Pos.CENTER);

        lblTotalPalomas = new Label("0");
        VBox cardTotal = crearCard("🕊️ Total de Palomas", lblTotalPalomas, "Palomas censadas", "#2563eb");

        lblVoladoras = new Label("0");
        VBox cardVol = crearCard("🚀 En Competición", lblVoladoras, "Palomas voladoras", "#059669");

        lblReproductoras = new Label("0");
        VBox cardRep = crearCard("🐣 En Reproducción", lblReproductoras, "Palomas de cría", "#d97706");

        lblCarreras = new Label("0");
        VBox cardCar = crearCard("🏁 Carreras / Sueltas", lblCarreras, "Eventos registrados", "#7c3aed");

        HBox.setHgrow(cardTotal, Priority.ALWAYS);
        HBox.setHgrow(cardVol, Priority.ALWAYS);
        HBox.setHgrow(cardRep, Priority.ALWAYS);
        HBox.setHgrow(cardCar, Priority.ALWAYS);

        hboxStats.getChildren().addAll(cardTotal, cardVol, cardRep, cardCar);

        // 3. Botonera de Acceso Rápido Gigante
        Label lblAccesos = new Label("⚡ Accesos Rápidos Principales");
        lblAccesos.getStyleClass().add("section-title");

        GridPane gridBotones = new GridPane();
        gridBotones.setHgap(20);
        gridBotones.setVgap(16);

        Button btnPalomas = crearBotonPrincipal("fas-dove", "Administrar Palomas", "Ver listado completo, árboles genealógicos y búsquedas", "btn-primary", () -> navegador.accept("PALOMAS"));
        Button btnNuevaPaloma = crearBotonPrincipal("fas-plus", "Registrar Nueva Paloma", "Agregar un nuevo anillo al plantel", "btn-success", accionNuevaPaloma);
        Button btnCarreras = crearBotonPrincipal("fas-flag-checkered", "Carreras y Resultados", "Ver sueltas, distancias y velocidades", "btn-secondary", () -> navegador.accept("CARRERAS"));
        Button btnPalomar = crearBotonPrincipal("fas-home", "Datos de Mi Palomar", "Editar propietario, ubicación y estirpes", "btn-secondary", () -> navegador.accept("PALOMAR"));

        gridBotones.add(btnPalomas, 0, 0);
        gridBotones.add(btnNuevaPaloma, 1, 0);
        gridBotones.add(btnCarreras, 0, 1);
        gridBotones.add(btnPalomar, 1, 1);

        // Responsive grid columns
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        gridBotones.getColumnConstraints().addAll(col1, col2);

        // 4. Pie con Estado del Sistema
        VBox cardEstado = new VBox(6);
        cardEstado.getStyleClass().add("stat-card");
        Label lblDbInfo = new Label("✅ Base de datos local SQLite activa (palomarg.db). Todos sus registros se guardan automáticamente en su computadora.");
        lblDbInfo.setStyle("-fx-font-size: 14px; -fx-text-fill: #15803d; -fx-font-weight: bold;");
        cardEstado.getChildren().add(lblDbInfo);

        getChildren().addAll(banner, hboxStats, lblAccesos, gridBotones, cardEstado);
    }

    private VBox crearCard(String titulo, Label lblValor, String descripcion, String colorHex) {
        VBox card = new VBox(8);
        card.getStyleClass().add("stat-card");
        card.setAlignment(Pos.CENTER_LEFT);

        Label lblTit = new Label(titulo);
        lblTit.getStyleClass().add("stat-title");

        lblValor.getStyleClass().add("stat-value");
        lblValor.setStyle("-fx-text-fill: " + colorHex + ";");

        Label lblDesc = new Label(descripcion);
        lblDesc.getStyleClass().add("stat-desc");

        card.getChildren().addAll(lblTit, lblValor, lblDesc);
        return card;
    }

    private Button crearBotonPrincipal(String iconoCodigo, String texto, String subtexto, String claseCss, Runnable accion) {
        Button btn = new Button();
        btn.getStyleClass().addAll(claseCss);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(75);
        
        FontIcon icono = new FontIcon(iconoCodigo);

        VBox content = new VBox(4);
        content.setAlignment(Pos.CENTER_LEFT);

        Label l1 = new Label(texto);
        l1.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: inherit;");

        Label l2 = new Label(subtexto);
        l2.setStyle("-fx-font-size: 14px; -fx-opacity: 0.9; -fx-text-fill: inherit;");
        
        content.getChildren().addAll(l1,l2);

        HBox fila = new HBox(14, icono, content);
        fila.setAlignment(Pos.CENTER_LEFT);
        btn.setGraphic(fila);
        btn.setOnAction(e -> accion.run());
        return btn;
    }

    public void recargarDatos() {
        int total = palomaRepo.countTotal();
        lblTotalPalomas.setText(String.valueOf(total));

        var todas = palomaRepo.findAll();
        long voladoras = todas.stream().filter(p -> p.getRol() == RolPaloma.VOLADORA).count();
        long reproductoras = todas.stream().filter(p -> p.getRol() == RolPaloma.REPRODUCTORA).count();

        lblVoladoras.setText(String.valueOf(voladoras));
        lblReproductoras.setText(String.valueOf(reproductoras));
        lblCarreras.setText(String.valueOf(carreraRepo.countTotal()));

        List<Palomar> palomares = palomarRepo.findAll();
        if (!palomares.isEmpty()) {
            Palomar p = palomares.get(0);
            lblBienvenidaPalomar.setText("👋 Bienvenido a PalomARG - " + p.getNombre() + " (" + p.getPropietario() + ")");
        }
    }
}
