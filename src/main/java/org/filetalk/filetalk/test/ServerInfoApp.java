package org.filetalk.filetalk.test;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ServerInfoApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Información del Servidor");

        // Crear una instancia de ServerInfo con datos de ejemplo
        ServerInfo serverInfo = new ServerInfo(
                "1.0.0", 8080, "45.160.14.99", "Wed Mar 19 17:14:35 CLST 2025",
                "Linux 6.13.6-200.fc41.x86_64", "21.0.6", 20, 15.64);

        // Crear etiquetas para mostrar información adicional
        Label waitingLabel = new Label("🔒  Esperando conexiones en el puerto " + serverInfo.getPort() + "...");
        waitingLabel.setStyle("-fx-font-weight: bold; -fx-padding: 10px;");

        // Crear una tabla para mostrar los detalles del servidor
        TableView<ServerInfo> tableView = new TableView<>();
        tableView.setEditable(false);

        // Definir columnas de la tabla
        TableColumn<ServerInfo, String> versionColumn = new TableColumn<>("Versión del Servidor");
        versionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getVersion()));

        TableColumn<ServerInfo, Integer> portColumn = new TableColumn<>("Puerto");
        portColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getPort()).asObject());

        TableColumn<ServerInfo, String> ipColumn = new TableColumn<>("IP");
        ipColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getIp()));

        TableColumn<ServerInfo, String> startTimeColumn = new TableColumn<>("Hora de Inicio");
        startTimeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStartTime()));

        TableColumn<ServerInfo, String> osColumn = new TableColumn<>("Sistema Operativo");
        osColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOs()));

        TableColumn<ServerInfo, String> javaVersionColumn = new TableColumn<>("Versión de Java");
        javaVersionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getJavaVersion()));

        TableColumn<ServerInfo, Integer> processorsColumn = new TableColumn<>("Procesadores Disponibles");
        processorsColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getAvailableProcessors()).asObject());

        TableColumn<ServerInfo, Double> memoryColumn = new TableColumn<>("Memoria Máxima Heap (GB)");
        memoryColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getMaxHeapMemory()).asObject());

        // Agregar las columnas a la tabla
        tableView.getColumns().addAll(versionColumn, portColumn, ipColumn, startTimeColumn, osColumn,
                javaVersionColumn, processorsColumn, memoryColumn);

        // Agregar los datos a la tabla
        tableView.getItems().add(serverInfo);

        // Crear un contenedor VBox y agregar los controles
        VBox vbox = new VBox(10, waitingLabel, tableView);
        vbox.setStyle("-fx-padding: 20px;");

        // Crear y mostrar la escena
        Scene scene = new Scene(vbox, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }


    class ServerInfo {
        private String version;
        private int port;
        private String ip;
        private String startTime;
        private String os;
        private String javaVersion;
        private int availableProcessors;
        private double maxHeapMemory;

        // Constructor
        public ServerInfo(String version, int port, String ip, String startTime, String os, String javaVersion,
                          int availableProcessors, double maxHeapMemory) {
            this.version = version;
            this.port = port;
            this.ip = ip;
            this.startTime = startTime;
            this.os = os;
            this.javaVersion = javaVersion;
            this.availableProcessors = availableProcessors;
            this.maxHeapMemory = maxHeapMemory;
        }

        // Métodos getter y setter
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }

        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }

        public String getIp() { return ip; }
        public void setIp(String ip) { this.ip = ip; }

        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }

        public String getOs() { return os; }
        public void setOs(String os) { this.os = os; }

        public String getJavaVersion() { return javaVersion; }
        public void setJavaVersion(String javaVersion) { this.javaVersion = javaVersion; }

        public int getAvailableProcessors() { return availableProcessors; }
        public void setAvailableProcessors(int availableProcessors) { this.availableProcessors = availableProcessors; }

        public double getMaxHeapMemory() { return maxHeapMemory; }
        public void setMaxHeapMemory(double maxHeapMemory) { this.maxHeapMemory = maxHeapMemory; }
    }
}
