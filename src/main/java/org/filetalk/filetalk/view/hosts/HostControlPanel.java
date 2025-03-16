package org.filetalk.filetalk.view.hosts;

import javafx.scene.layout.HBox;
import javafx.scene.control.*;
import javafx.geometry.Pos;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import org.filetalk.filetalk.Client.Client;
import org.filetalk.filetalk.Client.ClientInfo;

import java.io.File;
import java.io.IOException;

public class HostControlPanel extends Pane {
    private Label hostNameLabel;
    private Button sendFileButton; // Nuevo botón para enviar archivo
    private Client client;
    private  ClientInfo host;
    private Label connectionStatusLabel;
    private Circle connectionStatusCircle;
    private Label timerLabel;

    public HostControlPanel(ClientInfo host, Client cliente) {
        this.client=cliente;
        this.host=host;
        this.initGUI(host);

    }

    private void initGUI(ClientInfo host) {

        // Crear las etiquetas para el nombre y dirección del host
        this.hostNameLabel = new Label(host.getNick()+" "+host.getAddress());

        // Nuevo botón para enviar archivo
        this.sendFileButton = new Button("Enviar archivo");

        // Acción para el botón de enviar archivo
        this.sendFileButton.setOnAction(event -> performSendFileAction());

        // Crear un HBox para organizar los elementos horizontalmente
        HBox container = new HBox(10); // Espaciado de 10px entre los elementos
        container.setAlignment(Pos.CENTER_LEFT); // Alinear los elementos a la izquierda
        container.getChildren().addAll(hostNameLabel, sendFileButton);

        // Configuración de tamaño preferido para la etiqueta
        this.hostNameLabel.setPrefWidth(150); // Dar un tamaño adecuado al nombre del host
        this.sendFileButton.setPrefWidth(120); // Dar un tamaño adecuado al botón de enviar archivo

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

    // Método para manejar la acción de enviar archivo
    private void performSendFileAction()   {
        // Crear un FileChooser para seleccionar el archivo
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files", "*.*"));
        File selectedFile = fileChooser.showOpenDialog(new Stage());

        if (selectedFile != null) {
            // Aquí puedes agregar la lógica para enviar el archivo

            try {
                //System.out.println("/file " + host.getNick() + " " + selectedFile.getAbsolutePath());
                client.handleFileTransfer("/file " + host.getAddress() + " " + selectedFile.getAbsolutePath());

                long time=90;
                int minutes = (int) (time / 60);
                int seconds = (int) (time % 60);
                timerLabel.setText(String.format("Tiempo restante: %02d:%02d", minutes, seconds));

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Implementa la lógica de envío de archivo aquí, por ejemplo, usando sockets o alguna otra tecnología.
        } else {
            // Si no se seleccionó ningún archivo
            System.out.println("No se seleccionó ningún archivo.");
        }
    }

    // Método para habilitar o deshabilitar opciones del host
    public void enableHostOptions(boolean enable) {
        // Si se añaden más botones o controles, este método los habilitaría o deshabilitaría
        this.sendFileButton.setDisable(!enable);
    }
}
