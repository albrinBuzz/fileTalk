package org.filetalk.filetalk.view.tranferens;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.filetalk.filetalk.Client.TransferManager;
import org.filetalk.filetalk.models.Transferencia;

public class TransferenceControlPanel extends VBox {

    private ProgressBar progressBar;
    private Label titleAddrLabel;
    private Label addrLabel;
    private Label fileNameLabel;
    private Label modeLabel;
    private Button stopButton;
    private Button continueButton;
    private Button cancelButton;
    private TransferManager transferManager;
    private String mode;
    private boolean isPaused;
    private Label progressPercentageLabel;

    public TransferenceControlPanel(String mode, Transferencia transferencia, TransferManager transferManager) {
        this.mode = mode;
        this.transferManager = transferManager;
        this.isPaused = false;

        this.fileNameLabel = new Label(transferencia.getFileName());

        if (mode.equals("SENDING")) {
            this.titleAddrLabel = new Label("A destino:");
            this.modeLabel = new Label("[ Enviando ]");
            this.addrLabel = new Label(transferencia.getDstAddr());
        } else {
            this.titleAddrLabel = new Label("De origen:");
            this.modeLabel = new Label("[ Recibiendo ]");
            this.addrLabel = new Label(transferencia.getSrcAddr());
        }

        initGUI();
    }

    private void initGUI() {
        setModeColor();

        this.modeLabel.setPrefSize(90, 1);
        this.titleAddrLabel.setPrefSize(90, 20);
        this.titleAddrLabel.setStyle("-fx-text-fill: white;");
        this.addrLabel.setPrefSize(120, 20);
        this.fileNameLabel.setPrefSize(150, 20);

        this.fileNameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
        this.addrLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");

        this.progressBar = new ProgressBar(0);
        this.progressBar.setProgress(0);

        this.progressPercentageLabel = new Label("0%");
        this.progressPercentageLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
        this.progressPercentageLabel.setPrefSize(40, 20);

        this.stopButton = new Button("Detener");
        this.stopButton.setStyle("-fx-background-color: #ff6f61; -fx-text-fill: white; -fx-border-radius: 5px;");
        this.stopButton.setPrefSize(80, 30);
        this.stopButton.setOnAction(e -> stopTransference());

        this.continueButton = new Button("Continuar");
        this.continueButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-border-radius: 5px;");
        this.continueButton.setPrefSize(80, 30);
        this.continueButton.setOnAction(e -> continueTransference());
        this.continueButton.setDisable(true);

        this.cancelButton = new Button("Cancelar");
        this.cancelButton.setStyle("-fx-background-color: #757575; -fx-text-fill: white; -fx-border-radius: 5px;");
        this.cancelButton.setPrefSize(80, 30);
        this.cancelButton.setOnAction(e -> cancelTransference());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);
        grid.setAlignment(Pos.CENTER_LEFT);

        grid.add(this.modeLabel, 0, 0);

        HBox addrBox = new HBox(5, this.titleAddrLabel, this.addrLabel);
        addrBox.setAlignment(Pos.CENTER_LEFT);
        grid.add(addrBox, 1, 1);

        Label archivo = new Label("Nombre de archivo:");
        archivo.setStyle("-fx-text-fill: white;");
        grid.add(archivo, 0, 2);
        grid.add(this.fileNameLabel, 1, 2);

        Label progreso = new Label("Progreso:");
        progreso.setStyle("-fx-text-fill: white;");
        grid.add(progreso, 0, 3);
        grid.add(this.progressBar, 1, 3, 2, 1);
        grid.add(this.progressPercentageLabel, 3, 3);

        HBox buttonBox = new HBox(10, stopButton, continueButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        grid.add(buttonBox, 1, 4, 3, 1);

        this.getChildren().add(grid);

        this.setStyle("-fx-padding: 20; -fx-background-color: #2d2d2d; -fx-border-color: #00BFFF; -fx-border-width: 2;");
    }

    private void cancelTransference() {
        System.out.println("Transferencia cancelada.");
        // Aquí puedes implementar la lógica real de cancelación si es necesario
    }

    private void setModeColor() {
        String color;
        switch (this.mode) {
            case "SENDING":
                color = "#a01515";
                break;
            case "RECEIVING":
                color = "#1c964e";
                break;
            case "error":
                color = "#ff8c00";
                break;
            case "PAUSED":
                color = "#2196F3";
                break;
            case "pending":
                color = "#9C27B0";
                break;
            default:
                color = "#ffffff";
        }
        this.modeLabel.setStyle("-fx-text-fill: " + color + ";");
    }

    public void updateProgressBar(int progress) {
        Platform.runLater(() -> {
            this.progressBar.setProgress(progress / 100.0);
            this.progressPercentageLabel.setText(progress + "%");
        });
    }

    private void stopTransference() {
        this.isPaused = true;
        this.stopButton.setDisable(true);
        this.continueButton.setDisable(false);
        this.modeLabel.setText("[ Pausado ]");
        this.mode = "PAUSED";
        setModeColor();
        transferManager.pause();
    }

    private void continueTransference() {
        if (this.isPaused) {
            this.isPaused = false;
            this.stopButton.setDisable(false);
            this.continueButton.setDisable(true);
            this.modeLabel.setText("[ Enviando ]");
            this.mode = "SENDING";
            setModeColor();
            transferManager.resume();
        }
    }
}
