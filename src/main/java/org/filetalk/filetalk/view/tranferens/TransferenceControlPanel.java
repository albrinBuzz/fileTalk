package org.filetalk.filetalk.view.tranferens;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.filetalk.filetalk.Client.FileTransferManager;
import org.filetalk.filetalk.Client.TransferManager;


public class TransferenceControlPanel extends VBox {

    private final int MAX_WIDTH = 500;
    private ProgressBar progressBar;
    private Label titleAddrLabel;
    private Label addrLabel;
    private Label fileNameLabel;
    private Label modeLabel;
    private Button stopButton;
    private Button continueButton;
    private TransferManager transferManager;
    private String mode;
    private boolean isPaused;
    private Label progressPercentageLabel; // Etiqueta para mostrar el porcentaje

    // Constructor
    public TransferenceControlPanel(String mode, String src_addr, String dst_addr, String fileName, TransferManager transferManager) {
        this.mode = mode;

        this.fileNameLabel = new Label(fileName.substring(1));

        if (mode.equals("send")) {
            this.titleAddrLabel = new Label("A destino:");
            this.modeLabel = new Label("[ Enviando ]");
            this.addrLabel = new Label(dst_addr);
        } else {
            this.titleAddrLabel = new Label("De origen:");
            this.modeLabel = new Label("[ Recibiendo ]");
            this.addrLabel = new Label(src_addr);
        }
        this.transferManager=transferManager;

        this.isPaused = false;
        this.initGUI();
    }

    private void initGUI() {
        // Estilo del panel
        //this.setPrefWidth(MAX_WIDTH);
        //this.setMaxWidth(MAX_WIDTH);

        // Establecer el color de la etiqueta "modeLabel"
        this.setModeColor();

        this.modeLabel.setPrefSize(90, 1);
        this.titleAddrLabel.setPrefSize(90, 20);
        this.titleAddrLabel.setStyle("-fx-text-fill: white;");
        this.addrLabel.setPrefSize(120, 20);
        this.fileNameLabel.setPrefSize(150, 20);

        // Resaltar el nombre del archivo y la dirección
        this.fileNameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
        this.addrLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");

        // Barra de progreso
        this.progressBar = new ProgressBar(0);
        this.progressBar.setProgress(0); // Inicialmente en 0

        // Etiqueta para mostrar el porcentaje
        this.progressPercentageLabel = new Label("0%"); // Inicialmente 0%
        this.progressPercentageLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
        this.progressPercentageLabel.setPrefSize(40, 20);

        // Botón para detener la transferencia
        this.stopButton = new Button("Detener");
        this.stopButton.setStyle("-fx-background-color: #ff6f61; -fx-text-fill: white; -fx-border-radius: 5px;");
        this.stopButton.setPrefSize(80, 30);
        this.stopButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                stopTransference();
            }
        });

        // Botón para continuar la transferencia
        this.continueButton = new Button("Continuar");
        this.continueButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-border-radius: 5px;");
        this.continueButton.setPrefSize(80, 30);
        this.continueButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                continueTransference();
            }
        });
        this.continueButton.setDisable(true); // Inicialmente deshabilitado

        // Contenedor GridPane para organizar la información
        GridPane grid = new GridPane();
        grid.setHgap(10);  // Espaciado horizontal entre columnas
        grid.setVgap(5);   // Espaciado vertical entre filas
        grid.setAlignment(Pos.CENTER_LEFT);

        // Añadir los componentes al grid
        grid.add(this.modeLabel, 0, 0);
        //grid.add(new Label("Dirección:"), 0, 1);

        // Usamos un HBox para juntar "A destino" con la dirección
        HBox addrBox = new HBox(5, this.titleAddrLabel, this.addrLabel);
        addrBox.setAlignment(Pos.CENTER_LEFT);
        grid.add(addrBox, 1, 1);


        Label archivo= new Label("Nombre de archivo:");
        archivo.setStyle("-fx-text-fill: white;");
        grid.add(archivo, 0, 2);
        grid.add(this.fileNameLabel, 1, 2);
        Label progreso= new Label("Progreso");
        progreso.setStyle("-fx-text-fill: white;");
        grid.add(progreso, 0, 3);
        grid.add(this.progressBar, 1, 3, 2, 1);  // La barra de progreso ocupa dos columnas
        grid.add(this.progressPercentageLabel, 3, 3); // Colocar la etiqueta de porcentaje junto a la barra

        // Añadir botones
        HBox buttonBox = new HBox(10, stopButton, continueButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);  // Alinear los botones a la derecha
        grid.add(buttonBox, 1, 4, 2, 1);  // Los botones estarán en la fila 4

        // Agregar el grid al pane principal
        this.getChildren().add(grid);

        // Establecer tamaño y bordes del panel
        this.setPrefSize(530, 140);
        this.setMaxSize(530, 140);
        this.setMinSize(530, 140);
        this.setStyle("-fx-padding: 20; -fx-background-color: #2d2d2d; -fx-border-color: #00BFFF; -fx-border-width: 2;"); // Celeste en los márgenes del root
    }

    // Método para asignar el color dependiendo del modo
    private void setModeColor() {
        String color;
        switch (this.mode) {
            case "send":
                color = "#a01515";  // Rojo
                break;
            case "receive":
                color = "#1c964e";  // Verde
                break;
            case "error":
                color = "#ff8c00";  // Naranja (Error)
                break;
            case "paused":
                color = "#2196F3";  // Azul (Pausado)
                break;
            case "pending":
                color = "#9C27B0";  // Morado (Pendiente)
                break;
            default:
                color = "#000000";  // Color por defecto
                break;
        }
        this.modeLabel.setStyle("-fx-text-fill: " + color + ";");
    }

    // Actualizar la barra de progreso y el porcentaje
    public void updateProgressBar(int progress) {
        Platform.runLater(() -> {
            this.progressBar.setProgress(progress / 100.0);
            this.progressPercentageLabel.setText(progress + "%");  // Actualizar el porcentaje
        });
    }

    // Detener la transferencia
    private void stopTransference() {
        // Lógica para detener la transferencia
        this.isPaused = true;  // Marcamos como pausado

        // Desactivar el botón "Detener" y activar el botón "Continuar"
        this.stopButton.setDisable(true);
        this.continueButton.setDisable(false);
        this.modeLabel.setText("[ Pausado ]");
        System.out.println("Pausado");
        mode="paused";
        setModeColor();
        transferManager.pause();
    }

    // Continuar con la transferencia
    private void continueTransference() {
        if (this.isPaused) {
            // Lógica para continuar con la transferencia
            this.isPaused = false;  // Desmarcar como pausado
            this.modeLabel.setText("[ Enviando ]");
            System.out.println("Pausado");
            mode="send";
            setModeColor();

            // Reactivar el botón "Detener" y desactivar el botón "Continuar"
            this.stopButton.setDisable(false);
            this.continueButton.setDisable(true);
            transferManager.resume();


            // Aquí puedes agregar la lógica para continuar la transferencia real
        }
    }
}
