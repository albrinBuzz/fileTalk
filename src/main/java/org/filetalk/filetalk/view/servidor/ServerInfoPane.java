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
import org.filetalk.filetalk.Client.ClientInfo;
import org.filetalk.filetalk.model.Observers.ServerObserver;
import org.filetalk.filetalk.server.Server;
import org.filetalk.filetalk.test.ServerInfoApp;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class ServerInfoPane extends Pane implements ServerObserver {

    @FXML
    private Label lblServerStatus, lblVersion, lblIP,lblPORT, lblOS, lblJavaVersion, lblAvailableProcessors, lblMaxMemory, lblUptime, lblBytesSent, lblThreadsActive;
    @FXML
    private TextArea txtClientsInfo;
    @FXML
    private Button btnClearConsole, btnStartServer;
    private Server server;
    private long startTime;
    private boolean isServerRunning;
    private int bytesSent = 0;  // Simulando bytes enviados
    private int threadsActive = 5;  // Simulando hilos activos
    private TableView<ClientInfo> tableView;
    public ServerInfoPane() throws UnknownHostException {
        startTime = System.nanoTime();
        isServerRunning = false;
        initGUI();
        server=Server.getInstance();
        server.setServerObserver(this);
    }

    public void initGUI() throws UnknownHostException {
        VBox root = new VBox(15);
        root.setStyle("-fx-padding: 20; -fx-background-color: #f0f0f0;");

        // Título del Servidor
        Label titleLabel = new Label("🌐 Información del Servidor 🌐");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Estado del Servidor
        lblServerStatus = new Label("Estado: Apagado");
        lblVersion = new Label("Versión del Servidor: 1.0.0");
        lblIP = new Label("IP: " + InetAddress.getLocalHost().getHostAddress());
        lblPORT= new Label("Puerto: ");
        lblOS = new Label("Sistema Operativo: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        lblJavaVersion = new Label("Versión de Java: " + System.getProperty("java.version"));
        lblAvailableProcessors = new Label("Procesadores Disponibles: " + Runtime.getRuntime().availableProcessors());

        // Memoria máxima
        long maxMemory = Runtime.getRuntime().maxMemory() / (1024 * 1024 * 1024); // GB
        lblMaxMemory = new Label("Memoria Máxima Heap: " + maxMemory + " GB");

        // Tiempo de actividad
        lblUptime = new Label("Uptime: 00:00:00");

        // Información adicional
        lblBytesSent = new Label("Bytes Enviados: 0 Bytes");
        lblThreadsActive = new Label("Hilos Activos: 5");

        // Información de clientes conectados
        txtClientsInfo = new TextArea();
        txtClientsInfo.setEditable(false);
        txtClientsInfo.setStyle("-fx-font-family: monospace;");

        // Botón para limpiar consola
        btnClearConsole = new Button("Limpiar Consola");
        btnClearConsole.setOnAction(event -> clearConsole());

        // Botón para encender servidor
        btnStartServer = new Button("Encender Servidor");
        btnStartServer.setOnAction(event -> startServer());

        // Crear una tabla para mostrar los detalles del servidor
        tableView = new TableView<>();
        tableView.setEditable(false);

        // Definir columnas de la tabla
        TableColumn<ClientInfo, String> versionColumn = new TableColumn<>("IP Cliente");
        versionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAddress()));

        TableColumn<ClientInfo, String> portColumn = new TableColumn<>("Tiempo Conexión");
        portColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().formatUptime()));

        TableColumn<ClientInfo, String> ipColumn = new TableColumn<>("Nick");
        ipColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNick()));

        // Agregar las columnas a la tabla
        tableView.getColumns().addAll(versionColumn, portColumn, ipColumn);


        /*tableView.getItems().add(new ClientInfo("1.0.0", 9L, "cliente1"));
        tableView.getItems().add(new ClientInfo("1.0.0", 9, "cliente1"));
        tableView.getItems().add(new ClientInfo("1.0.0", 43, "cliente1"));*/


        // Agregar los datos a la tabla
        //tableView.getItems().add(serverInfo);

        // Layout para organizar la información del servidor y los clientes conectados
        HBox infoContainer = new HBox(30);
        VBox serverInfoBox = new VBox(15);
        VBox clientsInfoBox = new VBox(15);

        // Agregar las etiquetas de la información del servidor a serverInfoBox
        serverInfoBox.getChildren().addAll(
                titleLabel,
                btnStartServer,
                lblServerStatus,
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

            // Cambiar el texto del botón a "Detener Servidor" cuando el servidor esté encendido
            btnStartServer.setText("Detener Servidor");
            /*Thread thread=new Thread(()->{
                server.starServerCLI();
            });
            thread.start();*/


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
            lblPORT.setText("Puerto: "+server.getPORT());

            // Iniciar el ciclo de actualización de la interfaz
            //startUpdating();
        } else {
            // Detener el servidor
            stopServer();
        }
    }

    // Método para detener el servidor
    private void stopServer() {
        // Simulamos que el servidor se apaga
        updateServerStatus("Apagado");
        isServerRunning = false;

        // Cambiar el texto del botón a "Encender Servidor"
        btnStartServer.setText("Encender Servidor");

        // Ocultar toda la información del servidor
        setServerInfoVisible(false);
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

    // Método para actualizar la vista en un ciclo de vida del servidor (por ejemplo, un thread para actualizar información)
    public void startUpdating() {

        Service<Void> servicio = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {



                            while (isServerRunning) {
                                //Thread.sleep(1000);  // Actualiza cada segundo
                                updateUptime();  // Actualiza el uptime
                                // Simulación de actualización de Bytes enviados y hilos activos
                                bytesSent += 1024;  // Simulamos que se enviaron 1024 bytes
                                threadsActive = 5 + (int)(Math.random() * 5);  // Simulamos que el número de hilos activos cambia

                                // Actualizamos las etiquetas
                                //lblBytesSent.setText("Bytes Enviados: " + bytesSent + " Bytes");
                                //lblThreadsActive.setText("Hilos Activos: " + threadsActive);
                            }



                        return null;
                    }
                };
            }
        };

        servicio.start(); // Inicia el servicio

    }

    // Método para ocultar o mostrar las secciones de información adicional
    private void setServerInfoVisible(boolean visible) {
        lblUptime.setVisible(visible);
        lblBytesSent.setVisible(visible);
        lblThreadsActive.setVisible(visible);
        txtClientsInfo.setVisible(visible);
    }

    @Override
    public void updateClient(List< ClientInfo> clientInfoList, int threadsActive) {

        ObservableList<ClientInfo>clientInfos= FXCollections.observableArrayList(clientInfoList);

        this.tableView.getItems().clear();
        tableView.setItems(clientInfos);

        Platform.runLater(()-> this.lblThreadsActive.setText("Hilos Activos: "+threadsActive));
    }

    @Override
    public void updateUptime(String uptime) {

        Platform.runLater(()->this.lblUptime.setText(uptime));
    }

    @Override
    public void updateBytes(String bytes) {
      Platform.runLater(()->this.lblBytesSent.setText("Bytes Enviados: "+bytes));
    }
}
