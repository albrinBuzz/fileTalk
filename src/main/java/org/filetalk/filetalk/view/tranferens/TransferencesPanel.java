package org.filetalk.filetalk.view.tranferens;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.control.ScrollPane;
import org.filetalk.filetalk.Client.FileTransferManager;
import org.filetalk.filetalk.Client.TransferManager;
import org.filetalk.filetalk.model.Observers.TransferencesObserver;
import org.filetalk.filetalk.shared.FileTransferState;

import java.util.HashMap;
import java.util.Map;

public class TransferencesPanel extends ScrollPane implements TransferencesObserver {

    private final String SEND_MODE = "send";
    private final String RECEIVE_MODE = "receive";

    private final int MAX_WIDTH = 588;

    private VBox transferencesContentPanel;
    private HashMap<String, TransferenceControlPanel> clientTransferencesMap, serverTransferencesMap;

    public TransferencesPanel(){
        this.clientTransferencesMap = new HashMap<>();
        this.serverTransferencesMap = new HashMap<>();
        this.initGUI();
    }

    private void initGUI() {
        // Estilo general para el panel
        //this.setStyle("-fx-background-color: #f4f6f9; -fx-border-radius: 8px; -fx-padding: 20px; -fx-border-color: #e0e0e0;");

        // Contenedor principal con espaciado
        this.transferencesContentPanel = new VBox(10);
        //this.transferencesContentPanel.setStyle("-fx-background-color: #ffffff; -fx-border-radius: 8px; -fx-border-color: #e0e0e0; -fx-padding: 15px;");
        this.transferencesContentPanel.setPrefWidth(MAX_WIDTH);

        // Panel de "No hay transferencias"
        HBox infoPanel = new HBox(10);
        infoPanel.setAlignment(Pos.CENTER_LEFT);
        infoPanel.setStyle("-fx-background-color: #2e2e2e; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 2 0;");
        infoPanel.getChildren().add(createLabel("No hay transferencias en proceso", "#888888"));
        this.transferencesContentPanel.getChildren().add(infoPanel);
        this.setStyle("-fx-padding: 20; -fx-background-color: #2d2d2d; -fx-border-color: #00BFFF; -fx-border-width: 2;"); // Celeste en los márgenes del root

        setContent(transferencesContentPanel);

    }

    private void clearTransferences() {
        this.transferencesContentPanel.getChildren().clear();

        // Panel de información (sin transferencias)
        HBox infoPanel = new HBox(10);
        infoPanel.setAlignment(Pos.CENTER_LEFT);
        infoPanel.setStyle("-fx-background-color: #2e2e2e; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 2 0;");
        infoPanel.getChildren().add(createLabel("No hay transferencias en proceso", "#888888"));

        this.transferencesContentPanel.getChildren().add(infoPanel);
    }

    private Label createLabel(String text, String textColor) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 14px; -fx-font-family: 'Segoe UI', sans-serif;");
        return label;
    }

    @Override
    public void addTransference(String mode, String src_addr, String dst_addr, String fileName, TransferManager transferManager) {
        TransferenceControlPanel controlPanel = new TransferenceControlPanel(mode, src_addr, dst_addr, fileName, transferManager);
        if (mode.equals(SEND_MODE)) {
            this.clientTransferencesMap.put(dst_addr, controlPanel);
        } else if (mode.equals(RECEIVE_MODE)) {
            this.serverTransferencesMap.put(src_addr, controlPanel);
        }
        Platform.runLater(this::updateTransferenceContent);
    }

    @Override
    public void updateTransference(FileTransferState mode, String addr, int progress) {
        if (mode==FileTransferState.SENDING) {
            this.clientTransferencesMap.get(addr).updateProgressBar(progress);
        } else if (mode==FileTransferState.RECEIVING) {
            this.serverTransferencesMap.get(addr).updateProgressBar(progress);
        }
    }

    @Override
    public void endTransference(String mode, String addr) {
        if (mode.equals(SEND_MODE)) {
            this.clientTransferencesMap.remove(addr);
        } else if (mode.equals(RECEIVE_MODE)) {
            this.serverTransferencesMap.remove(addr);
        }

        if (this.clientTransferencesMap.isEmpty() && this.serverTransferencesMap.isEmpty()) {
            this.clearTransferences();
        } else {
            this.updateTransferenceContent();
        }
    }

    @Override
    public void notifyException(String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateTransferenceContent() {
        // Limpiar el contenido actual
        //this.transferencesContentPanel.getChildren().clear();

        // Crear un VBox para contener las barras de progreso
        VBox content = new VBox(10);
        content.setStyle("-fx-background-color: #ffffff;");

        // Agregar las transferencias del servidor
        for (Map.Entry<String, TransferenceControlPanel> mapElement : this.serverTransferencesMap.entrySet()) {
            content.getChildren().add(mapElement.getValue());
        }

        // Agregar las transferencias del cliente
        for (Map.Entry<String, TransferenceControlPanel> mapElement : this.clientTransferencesMap.entrySet()) {
            content.getChildren().add(mapElement.getValue());
        }

        // Colocar el contenido en un ScrollPane
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent;");

        this.transferencesContentPanel.getChildren().add(scrollPane);
    }
}
