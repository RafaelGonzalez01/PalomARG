package com.palomarg;

import com.palomarg.controller.MainController;
import com.palomarg.model.entity.Usuario;
import com.palomarg.view.LoginView;
import com.palomarg.view.MainView;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Clase principal de JavaFX para PalomARG.
 * Muestra LoginView antes de permitir el acceso a la MainView.
 */
public class PalomargApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Inicializar controlador MVC y repositorios
        MainController controller = new MainController();

        // Mostrar ventana de Login antes de la aplicación principal
        mostrarLogin(primaryStage, controller);
    }

    /**
     * Muestra la ventana de login. Si el usuario se autentica exitosamente,
     * abre la ventana principal. Si cierra sin autenticarse, cierra la app.
     */
    private void mostrarLogin(Stage primaryStage, MainController controller) {
        Stage loginStage = new Stage();
        loginStage.setTitle("PalomARG - Iniciar Sesión");
        loginStage.setResizable(false);
        loginStage.initModality(Modality.APPLICATION_MODAL);

        LoginView loginView = new LoginView(
            controller.getAuthService(),
            controller.getReclamoService(),
            controller.getColombofiloRepo()
        );

        // Al autenticarse exitosamente, abrir MainView
        loginView.setOnLoginExitoso(usuario -> {
            loginStage.close();
            abrirMainView(primaryStage, controller, usuario);
        });

        // Si el usuario cierra la ventana sin autenticarse, cerrar la app
        loginStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });

        Scene loginScene = new Scene(loginView, 520, 620);
        cargarCSS(loginScene);
        loginStage.setScene(loginScene);
        loginStage.show();
    }

    /**
     * Abre la ventana principal con el usuario autenticado.
     */
    private void abrirMainView(Stage primaryStage, MainController controller, Usuario usuario) {
        MainView mainView = new MainView(
            primaryStage,
            controller.getPalomaRepo(),
            controller.getOrigenRepo(),
            controller.getPalomarRepo(),
            controller.getCarreraRepo(),
            controller.getParticipacionRepo(),
            controller.getColombofiloRepo(),
            usuario,
            controller.getReclamoRepo(),
            controller.getReclamoService()
        );

        // Al cerrar sesión, volver a mostrar el login
        mainView.setOnCerrarSesion(() -> {
            primaryStage.hide();
            mostrarLogin(primaryStage, controller);
        });

        Scene scene = new Scene(mainView, 1100, 750);
        cargarCSS(scene);

        primaryStage.setTitle("PalomARG - Sistema Colombófilo de Gestión");
        primaryStage.setMinWidth(960);
        primaryStage.setMinHeight(650);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void cargarCSS(Scene scene) {
        try {
            String css = getClass().getResource("/css/styles.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception e) {
            System.err.println("Aviso: No se pudo cargar el archivo styles.css: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
