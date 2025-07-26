package org.filetalk.filetalk.Tests.Gui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.filetalk.filetalk.Client.TransferManager;
import org.filetalk.filetalk.model.Observers.TransferencesObserver;
import org.filetalk.filetalk.models.Transferencia;
import org.filetalk.filetalk.shared.FileTransferState;

public class TransferenciasView extends Application implements TransferencesObserver {


    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Transferencias en Curso");

        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(15));

        // Sección de transferencias en curso
        TitledPane enCursoPane = new TitledPane();
        enCursoPane.setText("🔄 Transferencias en curso (3)");
        VBox transferBox = new VBox(10);

        transferBox.getChildren().addAll(
                crearTransferencia("📤 imagen_producto.png", "Andrea Ruiz", 42, 2.1, 5, 400, "00:06:15", "Alta", "Envío"),
                crearTransferencia("📥 reporte_financiero.pdf", "Carlos Méndez", 73, 7.3, 10, 600, "00:02:45", "Normal", "Descarga"),
                crearTransferencia("📤 video_promocional.mp4", "Sara Cárdenas", 15, 1.5, 10, 200, "00:09:55", "Baja", "Envío"),
                crearTransferencia("📤 imagen_producto.png", "Andrea Ruiz", 42, 2.1, 5, 400, "00:06:15", "Alta", "Envío"),
                crearTransferencia("📥 reporte_financiero.pdf", "Carlos Méndez", 73, 7.3, 10, 600, "00:02:45", "Normal", "Descarga"),
                crearTransferencia("📤 video_promocional.mp4", "Sara Cárdenas", 15, 1.5, 10, 200, "00:09:55", "Baja", "Envío"),                crearTransferencia("📤 imagen_producto.png", "Andrea Ruiz", 42, 2.1, 5, 400, "00:06:15", "Alta", "Envío"),
                crearTransferencia("📥 reporte_financiero.pdf", "Carlos Méndez", 73, 7.3, 10, 600, "00:02:45", "Normal", "Descarga"),
                crearTransferencia("📤 video_promocional.mp4", "Sara Cárdenas", 15, 1.5, 10, 200, "00:09:55", "Baja", "Envío"),                crearTransferencia("📤 imagen_producto.png", "Andrea Ruiz", 42, 2.1, 5, 400, "00:06:15", "Alta", "Envío"),
                crearTransferencia("📥 reporte_financiero.pdf", "Carlos Méndez", 73, 7.3, 10, 600, "00:02:45", "Normal", "Descarga"),
                crearTransferencia("📤 video_promocional.mp4", "Sara Cárdenas", 15, 1.5, 10, 200, "00:09:55", "Baja", "Envío"),                crearTransferencia("📤 imagen_producto.png", "Andrea Ruiz", 42, 2.1, 5, 400, "00:06:15", "Alta", "Envío"),
                crearTransferencia("📥 reporte_financiero.pdf", "Carlos Méndez", 73, 7.3, 10, 600, "00:02:45", "Normal", "Descarga"),
                crearTransferencia("📤 video_promocional.mp4", "Sara Cárdenas", 15, 1.5, 10, 200, "00:09:55", "Baja", "Envío")
                );



        // Agregamos ScrollPane
        ScrollPane scrollPane = new ScrollPane(transferBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(300); // puedes ajustar la altura visible

        enCursoPane.setContent(scrollPane);
        enCursoPane.setExpanded(true);

        // Barra de búsqueda y filtros
        HBox busquedaBar = new HBox(10);
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

        // Historial reciente
        TitledPane historialPane = new TitledPane();
        historialPane.setText("🕘 Historial reciente");
        VBox historialBox = new VBox(5);
        historialBox.getChildren().addAll(
                new Label("✅ contrato_final.pdf - Enviado a Ana Torres (17/07/2025 10:22)"),
                new Label("❌ documento_confidencial.doc - Falló transferencia (16/07/2025 20:15)"),
                new Label("⏳ informe_ventas.xlsx - Pendiente aceptación (15/07/2025 18:45)"),
                new Label("✅ reporte_julio.pdf - Recibido de Andrea Ruiz (16/07/2025 18:10)")
        );
        historialPane.setContent(historialBox);

        HBox historialBotones = new HBox(10);
        historialBotones.getChildren().addAll(
                new Button("Reenviar seleccionado"),
                new Button("Eliminar seleccionado"),
                new Button("Ver detalles transferencia")
        );

        // Notificaciones
        TitledPane notificacionesPane = new TitledPane();
        notificacionesPane.setText("🔔 Notificaciones de Transferencias");
        VBox notifBox = new VBox(5);
        notifBox.getChildren().addAll(
                new Label("📩 Archivo recibido: \"reporte_julio.pdf\" de Andrea Ruiz"),
                new Label("⚠️ Transferencia \"documento_confidencial.doc\" fallida, reintentando..."),
                new Label("🔔 Transferencia \"imagen_producto.png\" pausada por baja velocidad de red")
        );
        notificacionesPane.setContent(notifBox);

        // Estadísticas
        TitledPane estadisticasPane = new TitledPane();
        estadisticasPane.setText("📊 Estadísticas Transferencias");
        Label stats = new Label("Archivos enviados: 120 (450 GB)\n" +
                "Archivos recibidos: 98 (360 GB)\n" +
                "Transferencias exitosas: 95%\n" +
                "Velocidad promedio: 500 KB/s");
        estadisticasPane.setContent(stats);

        mainLayout.getChildren().addAll(
                enCursoPane, busquedaBar,
                historialPane, historialBotones,
                notificacionesPane, estadisticasPane
        );

        Scene scene = new Scene(mainLayout, 900, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox crearTransferencia(String archivo, String usuario, int progreso, double enviadoMB, double totalMB, int velocidadKBs, String tiempoRestante, String prioridad, String tipo) {
        VBox box = new VBox(5);
        Label titulo = new Label(archivo + " - " + (tipo.equals("Envío") ? "Enviando a " : "Recibiendo de ") + usuario);
        ProgressBar barra = new ProgressBar(progreso / 100.0);
        Label info = new Label(String.format("Progreso: %d%%    %.1f MB / %.1f MB    Velocidad: %d KB/s    Tiempo restante: %s", progreso, enviadoMB, totalMB, velocidadKBs, tiempoRestante));
        Label meta = new Label("Prioridad: " + prioridad + "   | Tipo: " + tipo);
        HBox controles = new HBox(10, new Button("⏸️ Pausar"), new Button("▶️ Reanudar"), new Button("❌ Cancelar"));

        box.getChildren().addAll(titulo, barra, info, meta, controles);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-background-color: #f9f9f9;");
        return box;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void addTransference(String mode, Transferencia transferencia, TransferManager transferManager) {

    }

    @Override
    public void updateTransference(FileTransferState mode, String addr, int progress) {

    }

    @Override
    public void endTransference(String mode, String addr) {

    }

    @Override
    public void notifyException(String message) {

    }
}
