package org.filetalk.filetalk.view.servidor;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class ServerConfigApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("⚙️ Configuración del Servidor y Transferencias");

        // Contenedor principal con scroll
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20));
        scrollPane.setContent(mainContainer);

        // Sección: Parámetros de red
        TitledPane redPane = createParametrosRedPane();
        TitledPane almacenamientoPane = createAlmacenamientoPane();
        TitledPane transferenciasPane = createTransferenciasPane();
        TitledPane seguridadPane = createSeguridadPane();
        TitledPane avanzadoPane = createAvanzadoPane();

        // Estado del servidor
        VBox estadoServidor = createEstadoServidorPane();

        HBox bottomButtons = new HBox(10,
                new Button("Guardar configuración"),
                new Button("Cancelar")
        );

        mainContainer.getChildren().addAll(
                redPane, almacenamientoPane, transferenciasPane,
                seguridadPane, avanzadoPane, bottomButtons,
                new Separator(), estadoServidor
        );

        Scene scene = new Scene(scrollPane, 900, 800);
        //scene.getStylesheets().add(getClass().getResource("dark-theme.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/server-config-dark.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();

    }

    public Parent getRoot(){
        VBox layout = new VBox(10);
        Label titulo = new Label("⚙️ Configuración del Servidor y Transferencias");
        titulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #333;");

        // Contenedor principal con scroll
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20));
        scrollPane.setContent(mainContainer);

        // Sección: Parámetros de red
        TitledPane redPane = createParametrosRedPane();
        TitledPane almacenamientoPane = createAlmacenamientoPane();
        TitledPane transferenciasPane = createTransferenciasPane();
        TitledPane seguridadPane = createSeguridadPane();
        TitledPane avanzadoPane = createAvanzadoPane();

        // Estado del servidor
        VBox estadoServidor = createEstadoServidorPane();

        HBox bottomButtons = new HBox(10,
                new Button("Guardar configuración"),
                new Button("Cancelar")
        );

        mainContainer.getChildren().addAll(
                redPane, almacenamientoPane, transferenciasPane,
                seguridadPane, avanzadoPane, bottomButtons,
                new Separator(), estadoServidor
        );

        Scene scene = new Scene(scrollPane, 900, 800);
        //scene.getStylesheets().add(getClass().getResource("dark-theme.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/server-config-dark.css").toExternalForm());

        layout.getChildren().addAll(
                titulo,
                scrollPane
        );
        return layout;
    }

    private TitledPane createParametrosRedPane() {
        GridPane grid = new GridPane();
        grid.setVgap(8);
        grid.setHgap(10);

        int row = 0;
        grid.add(new Label("Puerto de conexión del servidor:"), 0, row);
        grid.add(new TextField("8080"), 1, row++);

        grid.add(new Label("Dirección IP (escuchar en):"), 0, row);
        grid.add(new TextField("0.0.0.0"), 1, row++);

        grid.add(new Label("Número máximo de conexiones:"), 0, row);
        grid.add(new TextField("50"), 1, row++);

        grid.add(new Label("Permitir conexiones externas:"), 0, row);
        grid.add(createRadioBox("Sí", "No", true), 1, row++);

        grid.add(new Label("Habilitar cifrado TLS/SSL:"), 0, row);
        grid.add(createRadioBox("Sí", "No", true), 1, row++);

        grid.add(new Label("Protocolo de transferencia preferido:"), 0, row);
        grid.add(createRadioBox("TCP", "UDP", false), 1, row++);

        TitledPane pane = new TitledPane("⚙️ Parámetros de red", grid);
        pane.setStyle("-fx-text-fill: white;");
        pane.setExpanded(false);
        return pane;
    }

    private TitledPane createAlmacenamientoPane() {
        GridPane grid = new GridPane();
        grid.setVgap(8);
        grid.setHgap(10);
        int row = 0;

        grid.add(new Label("Carpeta para guardar archivos recibidos:"), 0, row);
        Button seleccionarCarpeta = new Button("Seleccionar carpeta...");
        grid.add(seleccionarCarpeta, 1, row++);

        grid.add(new Label("Tamaño máximo de archivo permitido:"), 0, row);
        grid.add(new TextField("1 GB"), 1, row++);

        grid.add(new Label("Límite total de almacenamiento:"), 0, row);
        grid.add(new TextField("50 GB"), 1, row++);

        grid.add(new Label("Política de sobrescritura de archivos:"), 0, row);
        VBox opciones = new VBox(5,
                new RadioButton("No permitir sobrescribir archivos existentes"),
                new RadioButton("Sobrescribir si el archivo tiene el mismo nombre"),
                new RadioButton("Renombrar automáticamente para evitar sobrescribir")
        );
        ((RadioButton)opciones.getChildren().get(1)).setSelected(true);
        grid.add(opciones, 1, row++);

        grid.add(new Label("Mantener historial de archivos recibidos:"), 0, row);
        grid.add(createRadioBox("Sí", "No", true), 1, row++);

        grid.add(new Label("Número máximo de archivos en historial:"), 0, row);
        grid.add(new TextField("5000"), 1, row++);

        grid.add(new Label("Eliminar archivos automáticamente tras (días):"), 0, row);
        grid.add(new TextField("60"), 1, row++);

        TitledPane pane = new TitledPane("⚙️ Configuración de almacenamiento", grid);
        pane.setStyle("-fx-text-fill: white;");
        pane.setExpanded(false);
        return pane;
    }

    private TitledPane createTransferenciasPane() {
        GridPane grid = new GridPane();
        grid.setVgap(8);
        grid.setHgap(10);
        int row = 0;

        grid.add(new Label("Tiempo máximo de espera (segundos):"), 0, row);
        grid.add(new TextField("180"), 1, row++);

        grid.add(new Label("Retransmisiones automáticas en fallo:"), 0, row);
        grid.add(createRadioBox("Sí", "No", true), 1, row++);

        grid.add(new Label("Número máximo de reintentos:"), 0, row);
        grid.add(new TextField("5"), 1, row++);

        grid.add(new Label("Limitar velocidad de subida (KB/s):"), 0, row);
        grid.add(new TextField("4096"), 1, row++);

        grid.add(new Label("Limitar velocidad de bajada (KB/s):"), 0, row);
        grid.add(new TextField("4096"), 1, row++);

        grid.add(new Label("Tamaño del buffer (KB):"), 0, row);
        grid.add(new TextField("256"), 1, row++);

        grid.add(new Label("Transferencias simultáneas:"), 0, row);
        grid.add(new TextField("10"), 1, row++);

        grid.add(new Label("Notificar al usuario al completar:"), 0, row);
        grid.add(createRadioBox("Sí", "No", true), 1, row++);

        grid.add(new Label("Habilitar checksum (MD5/SHA256):"), 0, row);
        grid.add(createRadioBox("Sí", "No", true), 1, row++);

        grid.add(new Label("Permitir reanudación de transferencias:"), 0, row);
        grid.add(createRadioBox("Sí", "No", true), 1, row++);

        TitledPane pane = new TitledPane("⚙️ Transferencias", grid);
        pane.setStyle("-fx-text-fill: white;");
        pane.setExpanded(false);
        return pane;
    }

    private TitledPane createSeguridadPane() {
        GridPane grid = new GridPane();
        grid.setVgap(8);
        grid.setHgap(10);
        int row = 0;

        grid.add(new Label("Requerir autenticación para clientes:"), 0, row);
        grid.add(createRadioBox("Sí", "No", true), 1, row++);

        grid.add(new Label("Modo autenticación:"), 0, row);
        grid.add(createRadioBox("Simple", "Avanzada", false), 1, row++);

        grid.add(new Label("Habilitar lista blanca de IPs:"), 0, row);
        grid.add(createRadioBox("Sí", "No", false), 1, row++);

        grid.add(new Label("Lista blanca:"), 0, row);
        grid.add(new HBox(10, new TextField(), new Button("Editar...")), 1, row++);

        grid.add(new Label("Habilitar lista negra de IPs:"), 0, row);
        grid.add(createRadioBox("Sí", "No", false), 1, row++);

        grid.add(new Label("Lista negra:"), 0, row);
        grid.add(new HBox(10, new TextField(), new Button("Editar...")), 1, row++);

        grid.add(new Label("Activar notificaciones por email:"), 0, row);
        grid.add(createRadioBox("Sí", "No", true), 1, row++);

        grid.add(new Label("Email para notificaciones:"), 0, row);
        grid.add(new TextField("admin@ejemplo.com"), 1, row++);

        TitledPane pane = new TitledPane("⚙️ Seguridad y acceso", grid);
        pane.setStyle("-fx-text-fill: white;");
        pane.setExpanded(false);
        return pane;
    }

    private TitledPane createAvanzadoPane() {
        GridPane grid = new GridPane();
        grid.setVgap(8);
        grid.setHgap(10);
        int row = 0;

        grid.add(new Label("Registro detallado activado:"), 0, row);
        grid.add(createRadioBox("Sí", "No", true), 1, row++);

        grid.add(new Label("Ruta para guardar logs:"), 0, row);
        grid.add(new TextField("/var/log/transfer_app/"), 1, row++);

        grid.add(new Label("Nivel de detalle de logs:"), 0, row);
        VBox logLevels = new VBox(5,
                new RadioButton("Errores"),
                new RadioButton("Información"),
                new RadioButton("Debug")
        );
        ((RadioButton)logLevels.getChildren().get(1)).setSelected(true);
        grid.add(logLevels, 1, row++);

        grid.add(new Label("Programar mantenimiento automático:"), 0, row);
        grid.add(createRadioBox("Sí", "No", false), 1, row++);

        grid.add(new Label("Horario mantenimiento:"), 0, row);
        grid.add(new TextField("02:00 AM"), 1, row++);

        grid.add(new Label("Backup automático de configuración:"), 0, row);
        grid.add(createRadioBox("Sí", "No", true), 1, row++);

        grid.add(new Label("Ruta para backups:"), 0, row);
        grid.add(new TextField("/backups/transfer_app/"), 1, row++);

        TitledPane pane = new TitledPane("⚙️ Avanzado", grid);
        pane.setStyle("-fx-text-fill: white;");
        pane.setExpanded(false);
        return pane;
    }

    private VBox createEstadoServidorPane() {
        VBox estadoBox = new VBox(10);
        estadoBox.setPadding(new Insets(10));
        estadoBox.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-border-radius: 5;");

        Label estado = new Label("Estado: 🟢 Activo");
        Label usuarios = new Label("Usuarios conectados: 5");
        Label transferencias = new Label("Transferencias activas: 3");
        Label cpu = new Label("Uso de CPU: 28%");
        Label ram = new Label("Uso de RAM: 58%");
        Label velocidad = new Label("Velocidad red actual: 3.8 MB/s");
        Button verLog = new Button("Ver registro...");

        estadoBox.getChildren().addAll(
                new Label("📊 Estado del servidor"),
                new Separator(),
                estado,
                usuarios,
                transferencias,
                cpu,
                ram,
                velocidad,
                verLog
        );

        return estadoBox;
    }

    private HBox createRadioBox(String opcion1, String opcion2, boolean seleccionPrimera) {
        ToggleGroup group = new ToggleGroup();
        RadioButton rb1 = new RadioButton(opcion1);
        RadioButton rb2 = new RadioButton(opcion2);
        rb1.setToggleGroup(group);
        rb2.setToggleGroup(group);
        rb1.setSelected(seleccionPrimera);
        rb2.setSelected(!seleccionPrimera);
        return new HBox(10, rb1, rb2);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

