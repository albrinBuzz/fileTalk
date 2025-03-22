package org.filetalk.filetalk.view.servidor;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.filetalk.filetalk.Client.ClientInfo;
import org.filetalk.filetalk.model.Observers.ServerObserver;
import org.filetalk.filetalk.server.Server;
import org.filetalk.filetalk.test.ServerInfoApp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class ServerInfoPane extends Pane implements ServerObserver {

    @FXML
    private Label lblServerStatus, lblVersion, lblIP, lblPORT, lblOS, lblJavaVersion, lblAvailableProcessors, lblMaxMemory, lblUptime, lblBytesSent, lblThreadsActive;
    @FXML
    private TextArea txtClientsInfo;
    @FXML
    private Button btnClearConsole, btnStartServer;
    private Server server;
    private long startTime;
    private boolean isServerRunning;
    private TableView<ClientInfo> tableView;
    private Circle serverStatusCircle;

    public ServerInfoPane() throws UnknownHostException {
        startTime = System.nanoTime();
        isServerRunning = false;
        initGUI();
        server = Server.getInstance();
        server.setServerObserver(this);
    }

    public void initGUI() throws UnknownHostException {
        VBox root = new VBox(15);
        root.setStyle("-fx-padding: 20; -fx-background-color: #2d2d2d; -fx-border-color: #00BFFF; -fx-border-width: 2;"); // Celeste en los márgenes del root

        // Título del Servidor
        Label titleLabel = new Label("🌐 Información del Servidor 🌐");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Estado del Servidor
        lblServerStatus = new Label("Estado: Apagado");
        lblServerStatus.setStyle("-fx-text-fill: white;");

        serverStatusCircle = new Circle(10);
        serverStatusCircle.setFill(Color.RED); // Inicialmente rojo (apagado)



        lblVersion = new Label("Versión del Servidor: 1.0.0");
        lblVersion.setStyle("-fx-text-fill: white;");

        lblIP = new Label("IP: " + InetAddress.getLocalHost().getHostAddress());
        lblIP.setStyle("-fx-text-fill: white;");

        lblPORT = new Label("Puerto: ");
        lblPORT.setStyle("-fx-text-fill: white;");

        lblOS = new Label("Sistema Operativo: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        lblOS.setStyle("-fx-text-fill: white;");

        lblJavaVersion = new Label("Versión de Java: " + System.getProperty("java.version"));
        lblJavaVersion.setStyle("-fx-text-fill: white;");

        lblAvailableProcessors = new Label("Procesadores Disponibles: " + Runtime.getRuntime().availableProcessors());
        lblAvailableProcessors.setStyle("-fx-text-fill: white;");

        // Memoria máxima
        long maxMemory = Runtime.getRuntime().maxMemory() / (1024 * 1024 * 1024); // GB
        lblMaxMemory = new Label("Memoria Máxima Heap: " + maxMemory + " GB");
        lblMaxMemory.setStyle("-fx-text-fill: white;");

        lblUptime = new Label("Uptime: 00:00:00");
        lblUptime.setStyle("-fx-text-fill: white;");

        lblBytesSent = new Label("Bytes Enviados: 0 Bytes");
        lblBytesSent.setStyle("-fx-text-fill: white;");

        lblThreadsActive = new Label("Hilos Activos: 5");
        lblThreadsActive.setStyle("-fx-text-fill: white;");

        // Información de clientes conectados
        txtClientsInfo = new TextArea();
        txtClientsInfo.setEditable(false);
        txtClientsInfo.setStyle("-fx-font-family: monospace; -fx-background-color: #333333; -fx-text-fill: white;");

        // Botón para limpiar consola
        btnClearConsole = new Button("Limpiar Consola");
        btnClearConsole.setStyle("-fx-background-color: #444444; -fx-text-fill: white;");
        btnClearConsole.setOnAction(event -> clearConsole());

        // Botón para encender servidor
        btnStartServer = new Button("Encender Servidor");
        btnStartServer.setStyle("-fx-background-color: #444444; -fx-text-fill: white;");
        btnStartServer.setOnAction(event -> startServer());

        // Crear una tabla para mostrar los detalles del servidor
        tableView = new TableView<>();
        tableView.setEditable(false);
        tableView.setStyle("-fx-background-color: #333333; -fx-text-fill: white;");

// Definir columnas de la tabla
        TableColumn<ClientInfo, String> versionColumn = new TableColumn<>("IP Cliente");
        versionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAddress()));
        versionColumn.setStyle("-fx-background-color: black; -fx-text-fill: white;");  // Estilo de la columna

        TableColumn<ClientInfo, String> portColumn = new TableColumn<>("Tiempo Conexión");
        portColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().formatUptime()));
        portColumn.setStyle("-fx-background-color: black; -fx-text-fill: white;");  // Estilo de la columna

        TableColumn<ClientInfo, String> ipColumn = new TableColumn<>("Nick");
        ipColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNick()));
        ipColumn.setStyle("-fx-background-color: black; -fx-text-fill: white;");  // Estilo de la columna

// Agregar las columnas a la tabla
        tableView.getColumns().addAll(versionColumn, portColumn, ipColumn);

        // Layout para organizar la información del servidor y los clientes conectados
        HBox infoContainer = new HBox(30);
        VBox serverInfoBox = new VBox(15);
        VBox clientsInfoBox = new VBox(15);

        HBox serverStatusBox = new HBox(10);
        serverStatusBox.getChildren().addAll(lblServerStatus, serverStatusCircle);


        // Agregar las etiquetas de la información del servidor a serverInfoBox
        serverInfoBox.getChildren().addAll(
                titleLabel,
                btnStartServer,
                serverStatusBox,
                lblVersion,
                lblIP,
                lblPORT,
                lblOS,
                lblJavaVersion,
                lblAvailableProcessors,
                lblMaxMemory,
                lblUptime,
                new Separator(),
                lblBytesSent,
                lblThreadsActive,
                new Separator(),
                btnClearConsole
        );

        // Agregar la información de clientes conectados a clientsInfoBox
        clientsInfoBox.getChildren().addAll(
                new Label("Clientes Conectados:"),
                tableView
        );

        // Agregar ambos contenedores a infoContainer (lado a lado)
        infoContainer.getChildren().addAll(serverInfoBox, clientsInfoBox);
        infoContainer.setStyle("-fx-alignment: top-left;");

        // Agregar todo al root
        root.getChildren().add(infoContainer);

        this.getChildren().add(root);

        // Inicialmente ocultamos todas las secciones de información adicional
        setServerInfoVisible(false);
    }

    // Método para iniciar el servidor
    private void startServer() {
        if (!isServerRunning) {
            // Simulamos que el servidor se enciende
            updateServerStatus("Encendido");
            isServerRunning = true;
            serverStatusCircle.setFill(Color.GREEN);

            // Cambiar el texto del botón a "Detener Servidor" cuando el servidor esté encendido
            btnStartServer.setText("Detener Servidor");

            Service<Void> servicio = new Service<Void>() {
                @Override
                protected Task<Void> createTask() {
                    return new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            // Simulamos una operación de larga duración
                            server.startServer();
                            return null;
                        }
                    };
                }
            };

            servicio.start(); // Inicia el servicio

            // Mostrar toda la información del servidor
            setServerInfoVisible(true);
            lblPORT.setText("Puerto: " + server.getPORT());
        } else {
            // Detener el servidor
            try {
                stopServer();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // Método para detener el servidor
    private void stopServer() throws IOException {
        // Simulamos que el servidor se apaga
        updateServerStatus("Apagado");
        isServerRunning = false;

        try {
            server.stopServer();
        }catch (Exception e){

        }finally {
            // Cambiar el texto del botón a "Encender Servidor"
            btnStartServer.setText("Encender Servidor");
            serverStatusCircle.setFill(Color.RED);
            // Ocultar toda la información del servidor
            setServerInfoVisible(false);
        }

    }

    // Método para actualizar el uptime
    public void updateUptime() {
        if (isServerRunning) {
            long uptimeMillis = System.nanoTime() - startTime;
            long seconds = (uptimeMillis / 1000000000) % 60;
            long minutes = (uptimeMillis / (1000000000L * 60)) % 60;
            long hours = (uptimeMillis / (1000000000L * 60 * 60)) % 24;
            lblUptime.setText(String.format("Uptime: %02d:%02d:%02d", hours, minutes, seconds));
        }
    }

    // Método para actualizar el estado del servidor
    public void updateServerStatus(String status) {
        lblServerStatus.setText("Estado: " + status);
    }

    // Método para actualizar la lista de clientes conectados
    public void updateClientsInfo(String clientsInfo) {
        txtClientsInfo.setText(clientsInfo);
    }

    // Limpiar la consola
    private void clearConsole() {
        txtClientsInfo.clear();
    }

    // Método para ocultar o mostrar las secciones de información adicional
    private void setServerInfoVisible(boolean visible) {
        lblUptime.setVisible(visible);
        lblBytesSent.setVisible(visible);
        lblThreadsActive.setVisible(visible);
        txtClientsInfo.setVisible(visible);
    }

    @Override
    public void updateClient(List<ClientInfo> clientInfoList, int threadsActive) {
        ObservableList<ClientInfo> clientInfos = FXCollections.observableArrayList(clientInfoList);
        this.tableView.getItems().clear();
        tableView.setItems(clientInfos);

        Platform.runLater(() -> this.lblThreadsActive.setText("Hilos Activos: " + threadsActive));
    }

    @Override
    public void updateUptime(String uptime) {
        Platform.runLater(() -> this.lblUptime.setText(uptime));
    }

    @Override
    public void updateBytes(String bytes) {
        Platform.runLater(() -> this.lblBytesSent.setText("Bytes Enviados: " + bytes));
    }
}
