package org.filetalk.filetalk.Tests.Gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.filetalk.filetalk.Client.Client;
import org.filetalk.filetalk.Client.ClientInfo;
import org.filetalk.filetalk.Client.ConfiguracionCliente;
import org.filetalk.filetalk.Client.UtilidadesCliente;
import org.filetalk.filetalk.Tests.Gui.Transfers.TransferenciasView;
import org.filetalk.filetalk.Tests.Gui.hosts.HostsPanel;
import org.filetalk.filetalk.model.Observers.ServerObserver;
import org.filetalk.filetalk.server.Server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

public class MainView extends Application implements ServerObserver {


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
    private Label connectionStatusLabel;
    private Label connectionText;
    private Stage transfeStage; // Guarda la ventana actual

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        scene = new Scene(root, 1000, 800);
        client=new Client();
        controller = new MainController(this,client);
        hostsPanel = new HostsPanel(client);
        chatPanel=new ChatPanel(client);
        client.addObserver(chatPanel);
        transferenciasView = new TransferenciasView();
        client.getTransferenciaController()
                .setTransferencesObserver(transferenciasView);
        //controller.getClient().addObserver(chatPanel);

        // Inicializar el servidor
        server = Server.getInstance();
        server.setServerObserver(this);

        // Tema actual (oscuro o claro)
        StringProperty currentTheme = new SimpleStringProperty("Claro");

        // HEADER
        HBox header = new HBox(15);
        header.setPadding(new Insets(10));
        header.setStyle("-fx-background-color: #2c3e50;");

        Label userLabel = new Label("👤 Juan Ortega");
        userLabel.setTextFill(Color.WHITE);
        userLabel.setFont(Font.font(16));

        Label statusLabel = new Label("[🟢 Disponible ▼]");
        statusLabel.setTextFill(Color.LIGHTGREEN);
        statusLabel.setFont(Font.font(14));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button configBtn = new Button("⚙️ Configuración");

        Button securityBtn = new Button("🔒 Seguridad");
        Button logoutBtn = new Button("🚪 Cerrar sesión");

        header.getChildren().addAll(userLabel, statusLabel, spacer, configBtn, securityBtn, logoutBtn);

        // CONEXIÓN AL SERVIDOR
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



        connectionStatus.getChildren().addAll(connectionText, connectionStatusLabel,
                new Label("IP:"), ipField, new Label("Puerto:"), puertoField,
                latencyLabel, conectServerBtn, spacer2, serverLabel, autoConnectBtn,
                serverStatusLabel,  startServerBtn);





        // TOP MENU
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
            String temaPath = "/dark-theme.css"; // Usar la ruta desde la raíz del classpath
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

        topMenu.getChildren().addAll(searchUser, btnMisArchivos, btnEnviarArchivo, btnTransferencias,
                spacer3, temaLabel, temaCombo, personalizarLayout, idiomaLabel, idiomaCombo);



        // --------- Envio rápido de archivo --------------
        VBox envioRapidoBox = new VBox(5);
        envioRapidoBox.setPadding(new Insets(10));
        envioRapidoBox.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 1;");

        Label envioLabel = new Label("🚀 Envío rápido de archivo");
        envioLabel.setFont(Font.font(14));
        envioLabel.setStyle("-fx-font-weight: bold;");

        HBox destinatarioBox = new HBox(5);
        TextField destinatarioField = new TextField();
        destinatarioField.setPromptText("Destinatario");
        Button seleccionarUsuarioBtn = new Button("Seleccionar usuario ▼");
        destinatarioBox.getChildren().addAll(destinatarioField, seleccionarUsuarioBtn);

        HBox archivosBox = new HBox(5);
        TextField archivosField = new TextField();
        archivosField.setPromptText("Archivos");
        Button arrastrarBtn = new Button("Arrastrar y soltar archivos aquí");
        Button seleccionarArchivosBtn = new Button("Seleccionar archivos");
        archivosBox.getChildren().addAll(archivosField, arrastrarBtn, seleccionarArchivosBtn);

        TextField mensajeField = new TextField();
        mensajeField.setPromptText("Mensaje");

        HBox prioridadBox = new HBox(10);
        Label prioridadLabel = new Label("Prioridad:");
        ToggleGroup prioridadGroup = new ToggleGroup();
        RadioButton normalRb = new RadioButton("Normal");
        normalRb.setToggleGroup(prioridadGroup);
        normalRb.setSelected(true);
        RadioButton altaRb = new RadioButton("Alta");
        altaRb.setToggleGroup(prioridadGroup);
        RadioButton bajaRb = new RadioButton("Baja");
        bajaRb.setToggleGroup(prioridadGroup);
        prioridadBox.getChildren().addAll(prioridadLabel, normalRb, altaRb, bajaRb);

        HBox opcionesBox = new HBox(10);
        CheckBox enlaceDescargaCb = new CheckBox("Enviar como enlace de descarga");
        CheckBox cifradoCb = new CheckBox("Enviar cifrado personalizado");
        opcionesBox.getChildren().addAll(enlaceDescargaCb, cifradoCb);

        HBox programarEnvioBox = new HBox(10);
        DatePicker datePicker = new DatePicker();
        TextField horaField = new TextField();
        horaField.setPromptText("Hora (HH:mm)");
        programarEnvioBox.getChildren().addAll(new Label("Programar envío:"), datePicker, horaField);

        HBox botonesEnvioBox = new HBox(10);
        Button enviarBtn = new Button("Enviar archivo");
        Button cancelarBtn = new Button("Cancelar");
        botonesEnvioBox.getChildren().addAll(enviarBtn, cancelarBtn);

        envioRapidoBox.getChildren().addAll(envioLabel, destinatarioBox, archivosBox, mensajeField,
                prioridadBox, opcionesBox, programarEnvioBox, botonesEnvioBox);



        // --------- Últimas transferencias --------------
        VBox transferenciasBox = new VBox(5);
        transferenciasBox.setPadding(new Insets(10));
        transferenciasBox.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 1;");
        Label transferLabel = new Label("🔄 Últimas Transferencias ▸");
        transferLabel.setFont(Font.font(14));
        transferLabel.setStyle("-fx-font-weight: bold;");

        ListView<String> transferList = new ListView<>();
        transferList.getItems().addAll(
                "✅ contrato_final.pdf          enviado a Ana Torres           (17/07/2025 10:22)",
                "⏳ informe_ventas.xlsx         enviado a Carlos Méndez         (esperando aceptación)",
                "❌ documento_confidencial.doc  rechazado por Pablo Gil",
                "⏳ imagen_producto.png         transferencia activa                 40% [██████████░░░░░░░░░░░░] 2.0 MB / 4.8 MB  350 KB/s  00:08:45"
        );
        transferList.setPrefHeight(100);

        transferenciasBox.getChildren().addAll(transferLabel, transferList);

        // --------- Notificaciones --------------
        VBox notifBox = new VBox(5);
        notifBox.setPadding(new Insets(10));
        notifBox.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 1;");
        Label notifLabel = new Label("🔔 Notificaciones ▸");
        notifLabel.setFont(Font.font(14));
        notifLabel.setStyle("-fx-font-weight: bold;");

        ListView<String> notifList = new ListView<>();
        notifList.getItems().addAll(
                "📩 Has recibido \"reporte_julio.pdf\" de Andrea",
                "⚠️ Tu espacio está al 95% de uso",
                "🔔 Resumen semanal: 45 archivos enviados, 32 recibidos, 3 transferencias fallidas"
        );
        notifList.setPrefHeight(80);

        notifBox.getChildren().addAll(notifLabel, notifList);

        // --------- Estado del sistema --------------
        HBox estadoSistema = new HBox(15);
        estadoSistema.setPadding(new Insets(10));
        estadoSistema.setStyle("-fx-background-color: #ecf0f1;");

        Label usoLabel = new Label("📦 Uso: 5.9 GB / 6 GB");
        Label transHoyLabel = new Label("Transferencias hoy: 14");
        Label usuariosActLabel = new Label("Usuarios activos: 5/8");
        Label velRedLabel = new Label("📈 Velocidad red: 4.2 MB/s");
        Label cpuLabel = new Label("CPU: 35%");
        Label ramLabel = new Label("RAM: 62%");

        estadoSistema.getChildren().addAll(usoLabel, transHoyLabel, usuariosActLabel, velRedLabel, cpuLabel, ramLabel);

        // --------- Seguridad y privacidad --------------
        HBox seguridadBox = new HBox(15);
        seguridadBox.setPadding(new Insets(10));
        seguridadBox.setStyle("-fx-background-color: #ecf0f1;");

        Label usuariosBloqueadosLabel = new Label("Usuarios bloqueados: 2 [Ver lista]");
        Label auth2faLabel = new Label("Autenticación 2FA: Activada");
        Label sesionesActLabel = new Label("Sesiones activas: 1 dispositivo (PC)");

        seguridadBox.getChildren().addAll(usuariosBloqueadosLabel, auth2faLabel, sesionesActLabel);

        // --------- LAYOUT SETUP --------------
        VBox centerBox = new VBox(10);
        centerBox.setPadding(new Insets(5));
        centerBox.getChildren().addAll(hostsPanel, chatPanel);

        //centerBox.getChildren().addAll(hostsPanel, envioRapidoBox, chatPanel, transferenciasBox, notifBox);

        //centerBox.getChildren().addAll(usuariosScroll, envioRapidoBox, chatBox, transferenciasBox, notifBox);

        VBox mainBox = new VBox();
        mainBox.getChildren().addAll(header, connectionStatus, topMenu);

        ScrollPane mainScroll = new ScrollPane(centerBox);
        mainScroll.setFitToWidth(true);

        root.setTop(mainBox);
        root.setCenter(mainScroll);
        root.setBottom(new VBox(estadoSistema, seguridadBox));

        primaryStage.setTitle("App de Transferencias - Vista Principal");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

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
        System.out.println("Conectado");
        this.controller.connectServer(ipField.getText(),puertoField.getText());

    }


    private void openTransferenciasWindow() {
        // Crear una nueva ventana (Stage)
        /*Stage transferenciasStage = new Stage();

        transferenciasView.start(transferenciasStage);
        transferenciasStage.show();*/


        //Platform.runLater(() -> transferenciasView.main(new String[]{}));
        Parent root = transferenciasView.getRoot();
        Scene scene = new Scene(root, 900, 700);
        Stage transferenciasStage = new Stage();
        transferenciasStage.setScene(scene);
        transferenciasStage.show();


        // Muestra la nueva ventana

    }


    private void findServerAutomatically(TextField ipField, Label connectionStatusLabel) {
        new Thread(() -> {
            try {
                DatagramSocket socket = new DatagramSocket();
                socket.setBroadcast(true);

                // Enviar mensaje de descubrimiento
                DatagramPacket packet = new DatagramPacket(DISCOVERY_MESSAGE.getBytes(),
                        DISCOVERY_MESSAGE.length(), InetAddress.getByName("255.255.255.255"), DISCOVERY_PORT);
                socket.send(packet);

                // Esperar la respuesta del servidor
                byte[] buffer = new byte[1024];
                DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(responsePacket);

                String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
                if (response.startsWith("SERVER_IP:")) {
                    String serverIP = response.split(":")[1];
                    System.out.println("Servidor encontrado en la IP: " + serverIP);
                    // Actualizar el campo de IP
                    ipField.setText(serverIP);

                    // Actualizar el estado de la conexión
                    connectionStatusLabel.setText("[🟢 Conectado a " + serverIP + "]");
                    connectionStatusLabel.setTextFill(Color.LIGHTGREEN);
                } else {
                    // Si no se encuentra ningún servidor
                    connectionStatusLabel.setText("[🔴 No se pudo conectar]");
                    connectionStatusLabel.setTextFill(Color.RED);
                }

                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }
    private HBox createUserItem(String nombre, String estado, String ultimaConexion, boolean mostrarArchivos) {
        HBox userItem = new HBox(10);
        userItem.setPadding(new Insets(5));
        userItem.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 0 0 1 0;");
        userItem.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label(estado);
        Label nameLabel = new Label("👤 " + nombre);
        Label lastConn = new Label("Última conexión: " + ultimaConexion);
        lastConn.setStyle("-fx-font-style: italic;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button enviarArchivoBtn = new Button("📤 Enviar archivo");
        userItem.getChildren().addAll(nameLabel, icon, lastConn, spacer, enviarArchivoBtn);

        if (mostrarArchivos) {
            Button archivosBtn = new Button("📁 Archivos");
            userItem.getChildren().add(archivosBtn);
        }

        return userItem;
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

    @Override
    public void updateClient(List<ClientInfo> clientInfo, int threads) {

    }

    @Override
    public void updateUptime(String uptime) {

    }

    @Override
    public void updateMemory(String memory) {

    }

    @Override
    public void updateBytes(String bytes) {

    }
}
