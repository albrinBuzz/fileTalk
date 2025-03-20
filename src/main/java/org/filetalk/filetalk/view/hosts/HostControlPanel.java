package org.filetalk.filetalk.view.hosts;

import javafx.scene.layout.HBox;
import javafx.scene.control.*;
import javafx.geometry.Pos;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import org.filetalk.filetalk.Client.Client;
import org.filetalk.filetalk.Client.ClientInfo;

import java.io.File;
import java.io.IOException;

public class HostControlPanel extends Pane {
    private Label hostNameLabel;
    private Button sendFileButton;
    private Client client;
    private ClientInfo host;
    private Label connectionStatusLabel;
    private Circle connectionStatusCircle;
    private Label timerLabel;
    private ComboBox<String> selectionComboBox;

    public HostControlPanel(ClientInfo host, Client cliente) {
        this.client = cliente;
        this.host = host;
        this.initGUI(host);
    }

    private void initGUI(ClientInfo host) {
        // Crear las etiquetas para el nombre y dirección del host
        this.hostNameLabel = new Label(host.getNick() + " " + host.getAddress());

        // Crear un ComboBox para seleccionar el tipo de elemento a enviar
        this.selectionComboBox = new ComboBox<>();
        selectionComboBox.getItems().addAll("Enviar archivo", "Enviar directorio", "Enviar carpeta");
        selectionComboBox.setValue("Enviar archivo"); // Valor por defecto

        // Nuevo botón para enviar el elemento seleccionado
        this.sendFileButton = new Button("Enviar");

        // Acción para el botón de enviar
        this.sendFileButton.setOnAction(event -> performSendAction());

        // Crear un HBox para organizar los elementos horizontalmente
        HBox container = new HBox(10); // Espaciado de 10px entre los elementos
        container.setAlignment(Pos.CENTER_LEFT); // Alinear los elementos a la izquierda
        container.getChildren().addAll(hostNameLabel, selectionComboBox, sendFileButton);

        // Configuración de tamaño preferido para los elementos
        this.hostNameLabel.setPrefWidth(150);
        this.selectionComboBox.setPrefWidth(150);
        this.sendFileButton.setPrefWidth(100);

        // Estilo del panel: añadir fondo blanco y bordes opcionales
        this.setStyle("-fx-background-color: white; -fx-padding: 10;");
        this.connectionStatusCircle = new Circle(10);
        this.connectionStatusCircle.setFill(Color.RED); // Inicialmente desconectado

        // Agregar el círculo a la interfaz
        container.getChildren().add(connectionStatusCircle);

        // Ajuste de la etiqueta de estado
        this.connectionStatusLabel = new Label("Desconectado");
        container.getChildren().add(connectionStatusLabel);

        // Añadir el HBox al pane
        this.getChildren().add(container);

        this.timerLabel = new Label("Tiempo restante: 00:00");
        container.getChildren().add(timerLabel);

        // Configurar el tamaño máximo y preferido del pane
        this.setMaxSize(Region.USE_PREF_SIZE, 48);
        this.setPrefSize(500, 48);
    }

    public void updateConnectionStatus(boolean isConnected) {
        if (isConnected) {
            connectionStatusCircle.setFill(Color.GREEN);
            connectionStatusLabel.setText("Conectado");
        } else {
            connectionStatusCircle.setFill(Color.RED);
            connectionStatusLabel.setText("Desconectado");
        }
    }

    // Método para manejar la acción de enviar el elemento seleccionado
    private void performSendAction() {
        String selectedOption = selectionComboBox.getValue();
        File selectedFile = null;

        switch (selectedOption) {
            case "Enviar archivo":
                // Crear un FileChooser para seleccionar el archivo
                FileChooser fileChooser = new FileChooser();
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files", "*.*"));
                selectedFile = fileChooser.showOpenDialog(new Stage());
                break;
            case "Enviar directorio":
            case "Enviar carpeta":
                // Crear un DirectoryChooser para seleccionar el directorio
                DirectoryChooser directoryChooser = new DirectoryChooser();
                selectedFile = directoryChooser.showDialog(new Stage());

                break;
        }

        if (selectedFile != null) {
            try {
                // Lógica para enviar el archivo o directorio
                client.handleFileTransfer("/file " + host.getAddress() + " " + selectedFile.getAbsolutePath());

                // Actualizar el temporizador (ejemplo con 90 segundos)
                long time = 90;
                int minutes = (int) (time / 60);
                int seconds = (int) (time % 60);
                timerLabel.setText(String.format("Tiempo restante: %02d:%02d", minutes, seconds));

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Si no se seleccionó ningún archivo o directorio
            System.out.println("No se seleccionó ningún elemento.");
        }
    }

    // Método para habilitar o deshabilitar opciones del host
    public void enableHostOptions(boolean enable) {
        // Si se añaden más botones o controles, este método los habilitaría o deshabilitaría
        this.sendFileButton.setDisable(!enable);
        this.selectionComboBox.setDisable(!enable);
    }
}
