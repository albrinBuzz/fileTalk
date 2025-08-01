package org.filetalk.filetalk.view;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.filetalk.filetalk.Client.Client;
import org.filetalk.filetalk.Client.ConfiguracionCliente;
import org.filetalk.filetalk.Client.UtilidadesCliente;



import org.filetalk.filetalk.server.Server;
import org.filetalk.filetalk.view.hosts.ChatPanel;
import org.filetalk.filetalk.view.hosts.HostsPanel;
import org.filetalk.filetalk.view.servidor.ServerConfigApp;
import org.filetalk.filetalk.view.tranferens.EnvioAvanzadoPanel;
import org.filetalk.filetalk.view.tranferens.TransferenciasView;

import java.io.File;
import java.io.IOException;

public class MainView extends Application  {


    private MainController controller;
    private ComboBox<String> temaCombo;
    private Scene scene;
    private HostsPanel hostsPanel;
    private ChatPanel chatPanel;
    private static final int DISCOVERY_PORT = 8081;
    private static final String DISCOVERY_MESSAGE = "DISCOVER_SERVER";
    private Client client;
    private Server server; // Para manejar el servidor
    private boolean isServerRunning = false; // Estado del servidor
    private Label serverStatusLabel; // Etiqueta para mostrar el estado del servidor
    private Button startServerBtn;
    private TransferenciasView transferenciasView;
    private ServerConfigApp serverConfigView;
    private Label connectionStatusLabel;
    private Label connectionText;
    EnvioAvanzadoPanel panelEnvioAvanzado ;
    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        scene = new Scene(root, 1000, 800);
        client = new Client();
        controller = new MainController(this, client);
        hostsPanel = new HostsPanel(client);
        chatPanel = new ChatPanel(client);
        client.addObserver(chatPanel);
        panelEnvioAvanzado = new EnvioAvanzadoPanel();
        transferenciasView = new TransferenciasView();
        serverConfigView=new ServerConfigApp();
        client.getTransferenciaController().setTransferencesObserver(transferenciasView);

        server = Server.getInstance();

        // HEADER (igual que antes)
        HBox header = createHeader();

        // CONEXIÓN AL SERVIDOR (igual que antes)
        HBox connectionStatus = createConnectionStatus();

        // TOP MENU (igual que antes)
        HBox topMenu = createTopMenu();



        // USUARIOS CONECTADOS - Nueva sección sencilla y útil
        VBox usuariosConectadosBox = new VBox(8);
        usuariosConectadosBox.setPadding(new Insets(10));
        usuariosConectadosBox.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 1; -fx-background-color: #f7f9fa;");
        Label usuariosLabel = new Label("👥 Usuarios Conectados");
        usuariosLabel.setFont(Font.font("Segoe UI", 15));
        usuariosLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        ListView<String> usuariosList = new ListView<>();
        usuariosList.getItems().addAll(
                "🟢 Ana Torres [en línea]",
                "🟡 Pablo Gil [ausente]",
                "🟢 Carlos Méndez [en línea]"
        );
        usuariosList.setPrefHeight(100);
        usuariosConectadosBox.getChildren().addAll(usuariosLabel, usuariosList);

        // CENTER BOX
        VBox centerBox = new VBox(10);
        centerBox.setPadding(new Insets(5));
        centerBox.getChildren().addAll(hostsPanel,  chatPanel,panelEnvioAvanzado);

        // ESTADO DEL SISTEMA Y SEGURIDAD (igual que antes)
        HBox estadoSistema = createEstadoSistemaBox();
        HBox seguridadBox = createSeguridadBox();

        // AGRUPAR TOP CONTROLS
        VBox mainBox = new VBox();
        header.getStyleClass().add("main-header");
        connectionStatus.getStyleClass().add("connection-bar");
        mainBox.getChildren().addAll(header, connectionStatus, topMenu);

        ScrollPane mainScroll = new ScrollPane(centerBox);
        mainScroll.getStyleClass().add("main-scroll");
        mainScroll.setFitToWidth(true);

        root.setTop(mainBox);
        root.setCenter(mainScroll);

        estadoSistema.getStyleClass().add("bottom-section");
        seguridadBox.getStyleClass().add("bottom-section");
        //root.setBottom(new VBox(estadoSistema, seguridadBox));

        // Tema por defecto
        scene.getStylesheets().clear();
        scene.getStylesheets().add(getClass().getResource("/dark-theme.css").toExternalForm());

        primaryStage.setTitle("App de Transferencias - Vista Principal");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Ejemplo: método auxiliar para crear header (puedes poner los otros también)
    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setPadding(new Insets(10));
        header.setStyle("-fx-background-color: #2c3e50;");
        Label userLabel = new Label("👤 Juan Ortega");
        userLabel.setTextFill(Color.WHITE);
        userLabel.setFont(Font.font(16));
        Label statusLabel = new Label("[🟢 Offline ▼]");
        statusLabel.setTextFill(Color.RED);
        statusLabel.setFont(Font.font(14));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button configBtn = new Button("⚙️ Configuración");
        configBtn.setOnAction(event -> openConfigWindow());
        Button securityBtn = new Button("🔒 Seguridad");
        Button logoutBtn = new Button("🚪 Cerrar sesión");
        logoutBtn.setOnAction(event -> logut());
        header.getChildren().addAll(userLabel, statusLabel, spacer, configBtn, securityBtn, logoutBtn);
        return header;
    }

    private void logut() {
        this.client.desconect();
    }

// Similarmente crea createConnectionStatus(), createTopMenu(), createEnvioRapidoBox(),
// createEstadoSistemaBox(), createSeguridadBox() para limpiar el código



    private void descargas() {
        ConfiguracionCliente config = new ConfiguracionCliente();
        String rutaDescargas = config.obtener("cliente.directorio_descargas");

        new Thread(() -> {
            boolean exito = UtilidadesCliente.abrirDirectorioDescargas(rutaDescargas);
            if (!exito) {
                Platform.runLater(() -> {
                    // Mostrar un Alert si falla, desde el hilo de la UI
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("No se pudo abrir la carpeta de descargas.");
                    alert.setContentText("Verifica que la carpeta exista y tengas permisos.");
                    alert.showAndWait();
                });
            }
        }).start();
    }

    private void connectServer(TextField ipField, TextField puertoField) {

        this.controller.connectServer(ipField.getText(),puertoField.getText());

    }


    private void openTransferenciasWindow() {

        Parent root = transferenciasView.getRoot();
        Scene scene = new Scene(root, 900, 700);
        scene.getStylesheets().add(getClass().getResource("/dark-themeTrasf.css").toExternalForm());

        Stage transferenciasStage = new Stage();
        transferenciasStage.setScene(scene);
        transferenciasStage.show();


    }

    private void openConfigWindow() {

        Parent root = serverConfigView.getRoot();
        Scene scene = new Scene(root, 900, 700);
        scene.getStylesheets().add(getClass().getResource("/server-config-dark.css").toExternalForm());


        Stage transferenciasStage = new Stage();
        transferenciasStage.setScene(scene);
        transferenciasStage.show();


    }


    private HBox createConnectionStatus() {
        HBox connectionStatus = new HBox(10);
        connectionStatus.setPadding(new Insets(8));
        connectionStatus.setAlignment(Pos.CENTER_LEFT);
        connectionStatus.setStyle("-fx-background-color: #34495e;");

        connectionText = new Label("🔌 Estado de Conexión:");
        connectionText.setTextFill(Color.WHITE);

        connectionStatusLabel = new Label("[🟢 Desconectado]");
        connectionStatusLabel.setTextFill(Color.RED);

        TextField ipField = new TextField("192.168.100.111");
        ipField.setPrefWidth(130);
        TextField puertoField = new TextField("8080");
        puertoField.setPrefWidth(80);

        Label latencyLabel = new Label("Latencia: 35 ms");
        latencyLabel.setTextFill(Color.WHITE);

        Button conectServerBtn = new Button("🔄 conectar");
        conectServerBtn.setOnAction(e -> connectServer(ipField, puertoField));

        serverStatusLabel = new Label("[Servidor: Detenido]");
        serverStatusLabel.setTextFill(Color.RED);

        startServerBtn = new Button("🔛 Iniciar Servidor");
        startServerBtn.setOnAction(e -> controller.startServer());

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        Label serverLabel = new Label("Servidor: p2pfiles.example.com");
        serverLabel.setTextFill(Color.WHITE);

        Button autoConnectBtn = new Button("🔌 Conectar Automáticamente");
        autoConnectBtn.setOnAction(e -> findServerAutomatically(ipField, connectionStatusLabel));

        connectionStatus.getChildren().addAll(
                connectionText, connectionStatusLabel,
                new Label("IP:"), ipField,
                new Label("Puerto:"), puertoField,
                latencyLabel, conectServerBtn,
                spacer2, serverLabel, autoConnectBtn,
                serverStatusLabel, startServerBtn
        );

        return connectionStatus;
    }

    private HBox createTopMenu() {
        HBox topMenu = new HBox(10);
        topMenu.setPadding(new Insets(10));
        topMenu.setAlignment(Pos.CENTER_LEFT);

        TextField searchUser = new TextField();
        searchUser.setPromptText("Buscar usuario...");
        searchUser.setPrefWidth(250);

        Button btnMisArchivos = new Button("📁 Mis archivos");
        btnMisArchivos.setOnAction(e -> descargas());

        Button btnEnviarArchivo = new Button("📤 Enviar archivo");
        Button btnTransferencias = new Button("📤 Transferencias");
        btnTransferencias.setOnAction(e -> openTransferenciasWindow());

        Region spacer3 = new Region();
        HBox.setHgrow(spacer3, Priority.ALWAYS);

        ComboBox<String> temaCombo = new ComboBox<>();
        temaCombo.getItems().addAll("Claro", "Oscuro");
        temaCombo.setValue("Claro");

        temaCombo.setOnAction(e -> {
            String tema = temaCombo.getValue();
            String temaPath = "/dark-theme.css";
            if ("Oscuro".equals(tema)) {
                scene.getStylesheets().clear();
                scene.getStylesheets().add(getClass().getResource(temaPath).toExternalForm());
            } else {
                temaPath = "/light-theme.css";
                scene.getStylesheets().clear();
                scene.getStylesheets().add(getClass().getResource(temaPath).toExternalForm());
            }
        });

        Label temaLabel = new Label("Tema:");
        Button personalizarLayout = new Button("⚙️ Personalizar Layout");

        ComboBox<String> idiomaCombo = new ComboBox<>();
        idiomaCombo.getItems().addAll("Español", "Inglés");
        idiomaCombo.setValue("Español");

        Label idiomaLabel = new Label("Idioma:");

        topMenu.getChildren().addAll(
                searchUser, btnMisArchivos, btnEnviarArchivo, btnTransferencias,
                spacer3, temaLabel, temaCombo, personalizarLayout, idiomaLabel, idiomaCombo
        );

        return topMenu;
    }



    private HBox createEstadoSistemaBox() {
        HBox estadoSistema = new HBox(15);
        estadoSistema.setPadding(new Insets(10));

        Label usoLabel = new Label("📦 Uso: 5.9 GB / 6 GB");
        Label transHoyLabel = new Label("Transferencias hoy: 14");
        Label usuariosActLabel = new Label("Usuarios activos: 5/8");
        Label velRedLabel = new Label("📈 Velocidad red: 4.2 MB/s");
        Label cpuLabel = new Label("CPU: 35%");
        Label ramLabel = new Label("RAM: 62%");

        estadoSistema.getChildren().addAll(usoLabel, transHoyLabel, usuariosActLabel, velRedLabel, cpuLabel, ramLabel);
        return estadoSistema;
    }

    private HBox createSeguridadBox() {
        HBox seguridadBox = new HBox(15);
        seguridadBox.setPadding(new Insets(10));

        Label usuariosBloqueadosLabel = new Label("Usuarios bloqueados: 2 [Ver lista]");
        Label auth2faLabel = new Label("Autenticación 2FA: Activada");
        Label sesionesActLabel = new Label("Sesiones activas: 1 dispositivo (PC)");

        seguridadBox.getChildren().addAll(usuariosBloqueadosLabel, auth2faLabel, sesionesActLabel);
        return seguridadBox;
    }



    private void findServerAutomatically(TextField ipField, Label connectionStatusLabel) {


    }


    public Scene getScene() {
        return scene;
    }

    public ComboBox<String> getTemaCombo() {
        return temaCombo;
    }

    public Label getServerStatusLabel() {
        return serverStatusLabel;
    }

    public void setServerStatusLabel(Label serverStatusLabel) {
        this.serverStatusLabel = serverStatusLabel;
    }

    public boolean isServerRunning() {
        return isServerRunning;
    }

    public void setServerRunning(boolean serverRunning) {
        isServerRunning = serverRunning;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public Button getStartServerBtn() {
        return startServerBtn;
    }

    public Label getConnectionStatusLabel() {
        return connectionStatusLabel;
    }

    public static void main(String[] args) {
        launch(args);
    }


}
