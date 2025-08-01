package org.filetalk.filetalk.view.tranferens;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.filetalk.filetalk.Client.TransferManager;
import org.filetalk.filetalk.model.Observers.TransferencesObserver;
import org.filetalk.filetalk.models.Transferencia;
import org.filetalk.filetalk.shared.FileTransferState;
import org.filetalk.filetalk.shared.Logger;

import java.util.HashMap;

public class TransferenciasView extends Application implements TransferencesObserver {

    private  HashMap<String, TransferenceControlPanel> transferMap;
    private  VBox transferBox;
    private  TitledPane enCursoPane;
    private  HBox busquedaBar;
    private  TitledPane notificacionesPane;
    private  TitledPane estadisticasPane;
    private ScrollPane scrollTransa;

    public TransferenciasView() {
        this.transferMap = new HashMap<>();
        this.transferBox = new VBox(10);
        enCursoPane = new TitledPane();
        enCursoPane.setText("🔄 Transferencias en curso");
        this.busquedaBar = new HBox(10);
        this.notificacionesPane = new TitledPane();
        this.estadisticasPane = new TitledPane();
        transferBox.setStyle("-fx-background-color: #000000;");
        scrollTransa = new ScrollPane(transferBox);
        scrollTransa.setStyle(
                "-fx-background: #2a2a2a;" +
                        "-fx-background-color: #2a2a2a;" +
                        "-fx-control-inner-background: #2a2a2a;" +
                        "-fx-border-color: transparent;"
        );

        scrollTransa.setFitToWidth(true);
        scrollTransa.setPrefViewportHeight(300); // puedes ajustar la altura visible

        enCursoPane.setContent(scrollTransa);
        enCursoPane.setExpanded(true);
    }

    @Override
    public void start(Stage primaryStage) {

    }
    public Parent getRoot() {
        VBox layout = new VBox(10);
        // construir layout y contenido acá

        Label titulo = new Label("📁 Panel de Transferencias");
        titulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #333;");


        TextField buscador = new TextField();
        buscador.setPromptText("Buscar transferencias...");
        ComboBox<String> filtro = new ComboBox<>();
        filtro.getItems().addAll("Todos", "Envío", "Recepción", "Fallidos");
        filtro.setValue("Todos");

        ComboBox<String> ordenar = new ComboBox<>();
        ordenar.getItems().addAll("Fecha", "Nombre", "Tamaño");
        ordenar.setValue("Fecha");

        Button exportar = new Button("Exportar CSV");

        busquedaBar.getChildren().addAll(buscador, filtro, ordenar, exportar);

        // Notificaciones
        notificacionesPane = new TitledPane();
        notificacionesPane.setText("🔔 Notificaciones de Transferencias");
        VBox notifBox = new VBox(5);
        notifBox.getChildren().addAll(
                new Label("📩 Archivo recibido: \"reporte_julio.pdf\" de Andrea Ruiz"),
                new Label("⚠️ Transferencia \"documento_confidencial.doc\" fallida, reintentando..."),
                new Label("🔔 Transferencia \"imagen_producto.png\" pausada por baja velocidad de red")
        );
        notificacionesPane.setContent(notifBox);

        // Estadísticas
        estadisticasPane = new TitledPane();
        estadisticasPane.setText("📊 Estadísticas Transferencias");
        Label stats = new Label("Archivos enviados: 120 (450 GB)\n" +
                "Archivos recibidos: 98 (360 GB)\n" +
                "Transferencias exitosas: 95%\n" +
                "Velocidad promedio: 500 KB/s");
        estadisticasPane.setContent(stats);

        layout.getChildren().addAll(
                titulo,
                enCursoPane
        );
        return layout;
    }



    private void addTransferenceControlPanel(String mode, Transferencia transferencia, TransferManager transferManager) {
        TransferenceControlPanel controlPanel = new TransferenceControlPanel(mode, transferencia, transferManager);
        transferMap.put(transferencia.getId(), controlPanel);
        Platform.runLater(() -> transferBox.getChildren().add(controlPanel));
    }

    private void removeTransferenceControlPanel(String fileName) {
        TransferenceControlPanel controlPanel = transferMap.remove(fileName);
        Platform.runLater(() -> transferBox.getChildren().remove(controlPanel));
    }

    @Override
    public void addTransference(String mode, Transferencia transferencia, TransferManager transferManager) {

        Logger.logInfo("agregando la transferencia al observador");
        addTransferenceControlPanel(mode, transferencia,transferManager);
    }
    @Override
    public void updateTransference(FileTransferState mode, String id, int progress) {
        // Validación básica
        if (!transferMap.containsKey(id)) {
            Logger.logInfo("Transferencia con ID " + id + " no encontrada en el mapa.");
            return;
        }

        // Log detallado de la actualización
        /*Logger.logInfo(String.format(
                "Actualizando transferencia [%s] | Estado: %s | Progreso: %d%%",
                id, mode.name(), progress
        ));*/

        // Actualizar progreso según el estado
        if (mode == FileTransferState.SENDING || mode == FileTransferState.RECEIVING) {
            /*panel.updateProgressBar(
                    42,
                    "2.1 MB",
                    "5 MB",
                    "400 KB/s",
                    "00:06:15"
            );*/
            this.transferMap.get(id).updateProgressBar(progress);
        } else {
            Logger.logInfo("Estado de transferencia desconocido: " + mode);
        }
    }


    @Override
    public void endTransference(String mode, String addr) {
        // Cuando una transferencia termina, la eliminamos del mapa y del panel
        removeTransferenceControlPanel(addr);
    }

    @Override
    public void notifyException(String message) {
        // Mostrar una alerta en caso de que haya un error
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {

        launch(args);
    }
}
