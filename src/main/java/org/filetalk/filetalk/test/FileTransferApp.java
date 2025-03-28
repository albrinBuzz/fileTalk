package org.filetalk.filetalk.test;

import javafx.application.Application;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class FileTransferApp extends Application {

    private Random random = new Random();

    @Override
    public void start(Stage primaryStage) {
        // Main Layout
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f4f4f9;");

        // Header Section
        Text title = new Text("Sistema de Transferencia de Archivos");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        HBox header = new HBox(10);
        header.setStyle("-fx-padding: 20px;");
        header.getChildren().add(title);
        root.setTop(header);

        // Main Center Section with Sections
        VBox centerLayout = new VBox(20);
        centerLayout.setStyle("-fx-padding: 20px;");

        // 1. Historial de Transferencias (Combo de Historial)
        Label historialLabel = new Label("Historial de Transferencias:");
        ListView<String> historialList = new ListView<>();
        historialList.getItems().addAll("Archivo1.txt - Enviado", "Archivo2.jpg - Recibido");

        // 2. Encriptación de Archivos
        CheckBox encriptarCheckBox = new CheckBox("Habilitar Encriptación de Archivos");

        // 3. Transferencia de Archivos en Lotes
        Button seleccionarArchivosButton = new Button("Seleccionar Archivos");

        // 4. Prioridad de Transferencias
        ComboBox<String> prioridadComboBox = new ComboBox<>();
        prioridadComboBox.getItems().addAll("Alta", "Media", "Baja");

        // 5. Notificaciones de Estado (Progreso)
        Label estadoLabel = new Label("Estado de la Transferencia:");
        ProgressBar progresoTransferencia = new ProgressBar();
        progresoTransferencia.setProgress(0.5);  // Ejemplo de progreso

        // 6. Pausar/Reanudar Transferencia
        Button pausarButton = new Button("Pausar");
        Button reanudarButton = new Button("Reanudar");

        // 7. Vista Previa de Archivos
        Button vistaPreviaButton = new Button("Ver Vista Previa");

        // 8. Programar Transferencias
        DatePicker datePicker = new DatePicker();
        Button programarButton = new Button("Programar Transferencia");

        // 9. Control de Accesos y Autenticación
        TextField usuarioTextField = new TextField();
        PasswordField passwordField = new PasswordField();
        Button loginButton = new Button("Iniciar Sesión");

        // Servidor y Red Section (Simulación)
        // Estado del Servidor
        Label estadoServidorLabel = new Label("Estado del Servidor:");
        Label estadoServidor = new Label("Activo");
        estadoServidor.setTextFill(Color.GREEN);  // Estado activo

        // Uso de la Red
        Label usoRedLabel = new Label("Uso Actual de Red:");
        ProgressBar usoRedProgress = new ProgressBar();
        usoRedProgress.setProgress(0.3);  // Ejemplo de uso de red

        // Conexiones Activas
        Label conexionesActivasLabel = new Label("Conexiones Activas:");
        ListView<String> conexionesActivasList = new ListView<>();
        conexionesActivasList.getItems().addAll("Cliente 1 - Conectado", "Cliente 2 - Conectado");

        // Reiniciar Servidor
        Button reiniciarServidorButton = new Button("Reiniciar Servidor");

        // Grouping the sections
        centerLayout.getChildren().addAll(
                historialLabel, historialList,
                encriptarCheckBox,
                seleccionarArchivosButton,
                new Label("Prioridad de Transferencia:"), prioridadComboBox,
                estadoLabel, progresoTransferencia,
                new HBox(10, pausarButton, reanudarButton),
                vistaPreviaButton,
                new HBox(10, new Label("Programar Transferencia:"), datePicker, programarButton),
                new VBox(10, new Label("Control de Accesos:"), usuarioTextField, passwordField, loginButton),

                // Simulación de Servidor y Red
                new VBox(10, estadoServidorLabel, estadoServidor),
                new VBox(10, usoRedLabel, usoRedProgress),
                new VBox(10, conexionesActivasLabel, conexionesActivasList),
                reiniciarServidorButton,

                new HBox(20, new Button("Cambiar Tema"), new Button("Salir"))
        );

        root.setCenter(centerLayout);

        // Simulación en tiempo real
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
            // Simular cambios en el estado del servidor
            if (random.nextBoolean()) {
                estadoServidor.setText("Activo");
                estadoServidor.setTextFill(Color.GREEN);
            } else {
                estadoServidor.setText("Inactivo");
                estadoServidor.setTextFill(Color.RED);
            }

            // Simular uso de la red (valor entre 0 y 1)
            usoRedProgress.setProgress(random.nextDouble());

            // Simular actualizaciones de conexiones activas
            List<String> clientes = Arrays.asList("Cliente 1", "Cliente 2", "Cliente 3", "Cliente 4");
            List<String> clientesConectados = clientes.subList(0, random.nextInt(clientes.size()) + 1);
            conexionesActivasList.getItems().setAll(clientesConectados);

        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        // Configurar la escena y la ventana
        Scene scene = new Scene(root, 900, 650);
        primaryStage.setTitle("Aplicación de Transferencia de Archivos");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
