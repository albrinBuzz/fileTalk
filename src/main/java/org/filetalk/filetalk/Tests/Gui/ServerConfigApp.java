package org.filetalk.filetalk.Tests.Gui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import java.io.File;

public class ServerConfigApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Configuración del Servidor");

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        // Parámetros de red
        TitledPane redPane = new TitledPane();
        redPane.setText("⚙️ Parámetros de red");
        GridPane redGrid = new GridPane();
        redGrid.setVgap(10);
        redGrid.setHgap(10);

        TextField puertoServidor = new TextField("8080");
        TextField direccionIP = new TextField("0.0.0.0");
        TextField maxConexiones = new TextField("50");
        CheckBox permitirExternas = new CheckBox("Permitir conexiones externas");
        permitirExternas.setSelected(true);
        CheckBox tlsCheck = new CheckBox("Habilitar TLS/SSL");
        tlsCheck.setSelected(true);

        ToggleGroup protocoloGroup = new ToggleGroup();
        RadioButton tcp = new RadioButton("TCP");
        RadioButton udp = new RadioButton("UDP");
        udp.setSelected(true);
        tcp.setToggleGroup(protocoloGroup);
        udp.setToggleGroup(protocoloGroup);

        redGrid.addRow(0, new Label("Puerto de conexión:"), puertoServidor);
        redGrid.addRow(1, new Label("Dirección IP:"), direccionIP);
        redGrid.addRow(2, new Label("Máx. conexiones:"), maxConexiones);
        redGrid.addRow(3, permitirExternas);
        redGrid.addRow(4, tlsCheck);
        redGrid.addRow(5, new Label("Protocolo preferido:"), new HBox(10, tcp, udp));

        redPane.setContent(redGrid);
        redPane.setExpanded(true);

        // Configuración de almacenamiento
        TitledPane storagePane = new TitledPane();
        storagePane.setText("⚙️ Configuración de almacenamiento");
        GridPane storageGrid = new GridPane();
        storageGrid.setVgap(10);
        storageGrid.setHgap(10);

        TextField carpetaDestino = new TextField();
        Button seleccionarCarpeta = new Button("Seleccionar carpeta...");
        seleccionarCarpeta.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            File selectedDir = chooser.showDialog(primaryStage);
            if (selectedDir != null) {
                carpetaDestino.setText(selectedDir.getAbsolutePath());
            }
        });

        TextField maxArchivo = new TextField("1 GB");
        TextField limiteAlmacenamiento = new TextField("50 GB");

        storageGrid.addRow(0, new Label("Carpeta destino:"), carpetaDestino, seleccionarCarpeta);
        storageGrid.addRow(1, new Label("Tamaño máx. archivo:"), maxArchivo);
        storageGrid.addRow(2, new Label("Límite almacenamiento:"), limiteAlmacenamiento);

        storagePane.setContent(storageGrid);
        storagePane.setExpanded(true);

        // Botones
        HBox botones = new HBox(10);
        botones.setAlignment(Pos.CENTER_RIGHT);
        Button guardar = new Button("Guardar configuración");
        Button cancelar = new Button("Cancelar");
        botones.getChildren().addAll(guardar, cancelar);

        root.getChildren().addAll(redPane, storagePane, botones);

        Scene scene = new Scene(root, 700, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
