package org.filetalk.filetalk.Tests.Gui.Transfers;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.filetalk.filetalk.Client.TransferManager;
import org.filetalk.filetalk.models.Transferencia;
//import org.filetalk.filetalk.shared.//;

public class TransferenceControlPanel extends VBox {

    private ProgressBar progressBar;
    private Label fileNameLabel;
    private Label modeLabel;
    private Button stopButton;
    private Button continueButton;
    private TransferManager transferManager;
    private String mode;
    private boolean isPaused;
    Label progressPercentageLabel;
    // Constructor
    public TransferenceControlPanel(String mode, Transferencia transferencia, TransferManager transferManager) {
        this.mode = mode;
        this.fileNameLabel = new Label(transferencia.getFileName());
        this.modeLabel = new Label(mode);
        this.transferManager = transferManager;
        this.isPaused = false;
        this.initGUI();
    }

    private void initGUI() {
        //.logInfo("Iniciando initGUI()");

        try {

            // Barra de progreso
            this.progressBar = new ProgressBar(0);
            this.progressBar.setProgress(0);
            //.logInfo("Barra de progreso creada");

            // Etiqueta para mostrar el porcentaje
             progressPercentageLabel = new Label("0%");
            //.logInfo("Etiqueta de porcentaje creada");

            // Botón para detener la transferencia
            this.stopButton = new Button("Detener");
            this.stopButton.setOnAction(e -> stopTransference());
            //.logInfo("Botón detener creado y configurado");

            // Botón para continuar la transferencia
            this.continueButton = new Button("Continuar");
            this.continueButton.setDisable(true);
            this.continueButton.setOnAction(e -> continueTransference());
            //.logInfo("Botón continuar creado y configurado");

            // Contenedor GridPane para organizar la información
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(5);
            grid.setAlignment(Pos.CENTER_LEFT);
            //.logInfo("GridPane creado");

            // Agregar los componentes al grid
            grid.add(new Label("Modo:"), 0, 0);
            grid.add(this.modeLabel, 1, 0);
            //.logInfo("Fila modo agregada");

            grid.add(new Label("Archivo:"), 0, 1);
            grid.add(this.fileNameLabel, 1, 1);
            //.logInfo("Fila archivo agregada");

            grid.add(new Label("Progreso:"), 0, 2);
            grid.add(this.progressBar, 1, 2, 2, 1);
            grid.add(progressPercentageLabel, 3, 2);
            //.logInfo("Fila progreso agregada");

            // Botones de control
            HBox buttonBox = new HBox(10, stopButton, continueButton);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            grid.add(buttonBox, 1, 3, 2, 1);
            //.logInfo("Botones agregados al grid");

            this.getChildren().add(grid);
            //.logInfo("Grid agregado al panel");

            //.logInfo("panel de la transferencia creado correctamente");

        } catch (Exception e) {
            //.logInfo("Error en initGUI(): " + e.getMessage());
            e.printStackTrace();  // Opcional: para más detalles en consola
        }
    }


    public void updateProgressBar(int progress) {
        Platform.runLater(() -> {
            // Actualiza etiqueta de porcentaje
            this.progressPercentageLabel.setText(progress + "%");

            // Actualiza barra de progreso
            this.progressBar.setProgress(progress / 100.0);
        });
    }


    private void stopTransference() {
        this.isPaused = true;
        this.stopButton.setDisable(true);
        this.continueButton.setDisable(false);
        this.modeLabel.setText("[ Pausado ]");
        this.transferManager.pause();
    }

    private void continueTransference() {
        this.isPaused = false;
        this.stopButton.setDisable(false);
        this.continueButton.setDisable(true);
        this.modeLabel.setText("[ Enviando ]");
        this.transferManager.resume();
    }
}
