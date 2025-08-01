package org.filetalk.filetalk.view.tranferens;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import org.filetalk.filetalk.Client.TransferManager;
import org.filetalk.filetalk.models.Transferencia;


public class TransferenceControlPanel extends VBox {

    private ProgressBar progressBar;
    private Label progressPercentageLabel;
    private Label fileNameLabel;
    private Label directionLabel;
    private Label detailLabel;
    private Label priorityLabel;
    private Label speedLabel;
    private Label timeRemainingLabel;
    private Label typeLabel;
    private Button pauseButton;
    private Button resumeButton;
    private Button cancelButton;

    private boolean isPaused = false;
    private TransferManager transferManager;
    private String mode;

    public TransferenceControlPanel(String mode, Transferencia transferencia, TransferManager transferManager) {
        this.mode = mode;
        this.transferManager = transferManager;
        initUI(transferencia);
        //Logger.logInfo("interfaz de la transferencia creada");
    }

    private void initUI(Transferencia transferencia) {
        setSpacing(10);
        setPadding(new Insets(15));
        setStyle("-fx-background-color: #2d2d2d; -fx-border-color: #3a3a3a; -fx-border-width: 1; -fx-background-radius: 5px;");

        String directionText = mode.equals("SENDING") ?
                "📤 " + transferencia.getFileName() + "   Enviando a " + transferencia.getDstAddr()
                : "📥 " + transferencia.getFileName() + "   Recibiendo de " + transferencia.getSrcAddr();

        Label title = new Label(directionText);
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #ffffff; -fx-font-size: 14px;");

        // Progreso
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(400);
        progressBar.setStyle("-fx-accent: #00BFFF;");

        progressPercentageLabel = new Label("0%");
        progressPercentageLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold;");

        detailLabel = new Label("0 MB / 0 MB    Velocidad: 0 KB/s    Tiempo restante: --:--:--");
        detailLabel.setStyle("-fx-text-fill: #bbbbbb;");

        typeLabel = new Label("Tipo: " + (mode.equals("SENDING") ? "Envío" : "Descarga"));
        typeLabel.setStyle("-fx-text-fill: #dddddd;");

        HBox infoBox = new HBox(20, typeLabel);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        // Botones
        pauseButton = new Button("⏸️ Pausar");
        pauseButton.setOnAction(e -> pauseTransfer());
        pauseButton.setStyle("-fx-background-color: #d97706; -fx-text-fill: white;");
        pauseButton.setPrefWidth(90);

        resumeButton = new Button("▶️ Reanudar");
        resumeButton.setOnAction(e -> resumeTransfer());
        resumeButton.setStyle("-fx-background-color: #16a34a; -fx-text-fill: white;");
        resumeButton.setDisable(true);
        resumeButton.setPrefWidth(90);

        cancelButton = new Button("❌ Cancelar");
        cancelButton.setOnAction(e -> cancelTransfer());
        cancelButton.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;");
        cancelButton.setPrefWidth(90);

        HBox buttonBox = new HBox(10, pauseButton, resumeButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(title, createProgressPane(), detailLabel, infoBox, buttonBox);
    }


    private HBox createProgressPane() {
        HBox box = new HBox(10, progressBar, progressPercentageLabel);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    public void updateProgressBar(int progress) {
        Platform.runLater(() -> {
            double progressValue = progress / 100.0;
            progressBar.setProgress(progressValue);
            progressPercentageLabel.setText(progress + "%");
            //detailLabel.setText(transferred + " / " + total + "    Velocidad: " + speed + "    Tiempo restante: " + timeRemaining);
        });
    }

    private void pauseTransfer() {
        isPaused = true;
        pauseButton.setDisable(true);
        resumeButton.setDisable(false);
        transferManager.pause();
    }

    private void resumeTransfer() {
        if (isPaused) {
            isPaused = false;
            pauseButton.setDisable(false);
            resumeButton.setDisable(true);
            transferManager.resume();
        }
    }

    private void cancelTransfer() {
        System.out.println("Transferencia cancelada.");
        // Lógica real aquí
    }
}
