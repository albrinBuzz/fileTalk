package org.filetalk.filetalk.Tests.Gui.hosts;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.filetalk.filetalk.Client.Client;
import org.filetalk.filetalk.Client.ClientInfo;

import java.io.File;
import java.io.IOException;

public class HostControlPanel extends VBox {
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

        this.setSpacing(5);
        this.setPadding(new Insets(5));

        // Puedes personalizar estos detalles (status, tiempo, etc.) según tu necesidad
        /*Label usernameLabel = new Label(username);
        usernameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ffffff;");
        userBox.getChildren().add(usernameLabel);*/

        // Aquí se podría agregar un label para mostrar el estado de conexión, tiempo, etc.
        Label statusLabel = new Label("🟢 Online");  // Ejemplo de estado
        statusLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #888;");


        // Crear las etiquetas para el nombre y dirección del host
        //this.hostNameLabel = new Label(host.getNick() + " " + host.getAddress());
        //hostNameLabel.setStyle("-fx-text-fill: white;");
        this.hostNameLabel = new Label(host.getNick());
        hostNameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ffffff;");
        this.getChildren().add(statusLabel);

        // Crear un ComboBox para seleccionar el tipo de elemento a enviar
        this.selectionComboBox = new ComboBox<>();
        selectionComboBox.getItems().addAll("Enviar archivo", "Enviar carpeta");
        selectionComboBox.setValue("Enviar archivo"); // Valor por defecto
        selectionComboBox.setStyle("-fx-text-fill: white;");

        // Nuevo botón para enviar el elemento seleccionado
        this.sendFileButton = new Button("Enviar");
        sendFileButton.setStyle("-fx-text-fill: white; -fx-background-color: #333333;");

        // Acción para el botón de enviar
        this.sendFileButton.setOnAction(event -> {
            try {
                performSendAction();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // Crear un HBox para organizar los elementos horizontalmente
        HBox container = new HBox(10); // Espaciado de 10px entre los elementos
        container.setAlignment(Pos.CENTER_LEFT); // Alinear los elementos a la izquierda
        container.getChildren().addAll(hostNameLabel, selectionComboBox, sendFileButton);

        // Configuración de tamaño preferido para los elementos
        //this.hostNameLabel.setPrefWidth(150);
        //this.selectionComboBox.setPrefWidth(150);
        //this.sendFileButton.setPrefWidth(100);

        // Estilo del panel: añadir fondo oscuro y bordes opcionales
        this.setStyle("-fx-background-color: #2e2e2e; -fx-padding: 10;");
        this.connectionStatusCircle = new Circle(10);
        this.connectionStatusCircle.setFill(Color.GREEN); // Inicialmente desconectado

        // Agregar el círculo a la interfaz
        container.getChildren().add(connectionStatusCircle);

        // Ajuste de la etiqueta de estado
        this.connectionStatusLabel = new Label("Conectado");
        connectionStatusLabel.setStyle("-fx-text-fill: white;");
        container.getChildren().add(connectionStatusLabel);

        // Añadir el HBox al pane
        this.getChildren().add(container);


        // Configurar el tamaño máximo y preferido del pane
        //this.setMaxSize(Region.USE_PREF_SIZE, 48);
        //this.setPrefSize(500, 48);
        this.setStyle("-fx-padding: 20; -fx-background-color: #2d2d2d; -fx-border-color: #00BFFF; -fx-border-width: 2;"); // Celeste en los márgenes del root
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
    private void performSendAction() throws IOException {
        String selectedOption = selectionComboBox.getValue();
        File selectedFile = null;

        switch (selectedOption) {
            case "Enviar archivo":
                // Crear un FileChooser para seleccionar el archivo
                FileChooser fileChooser = new FileChooser();
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files", "*.*"));
                selectedFile = fileChooser.showOpenDialog(new Stage());

                //Logger.logInfo("Enviado Archivo");
                if (selectedFile!=null){

                    //client.handleFileTransfer("/file " + host.getAddress() + " " + selectedFile.getAbsolutePath(),host.getAddress(),host.getPort());
                    client.handleFileTransfer("/file " + host.getNick() + " " + selectedFile.getAbsolutePath(), client.getSERVER_ADDRESS(), client.getSERVER_PORT());
                }
                break;

            case "Enviar carpeta":
                // Crear un DirectoryChooser para seleccionar el directorio
                DirectoryChooser directoryChooser = new DirectoryChooser();
                selectedFile = directoryChooser.showDialog(new Stage());

                if (selectedFile != null) {
                    client.handleDirectoryTransfer(selectedFile.getAbsolutePath(),client.getSERVER_ADDRESS(),client.getSERVER_PORT(),host.getNick());
                }
                break;
        }

        /*if (selectedFile != null) {
            try {
                // Lógica para enviar el archivo o directorio

                System.out.println("Direccion del host: "+host.getAddress());
                client.handleFileTransfer("/file " + host.getAddress() + " " + selectedFile.getAbsolutePath(),host.getAddress());


                // Actualizar el temporizador (ejemplo con 90 segundos)
                long time = 90;
                int minutes = (int) (time / 60);
                int seconds = (int) (time % 60);
                //timerLabel.setText(String.format("Tiempo restante: %02d:%02d", minutes, seconds));

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Si no se seleccionó ningún archivo o directorio
            System.out.println("No se seleccionó ningún elemento.");
        }*/
    }

    // Método para habilitar o deshabilitar opciones del host
    public void enableHostOptions(boolean enable) {
        // Si se añaden más botones o controles, este método los habilitaría o deshabilitaría
        this.sendFileButton.setDisable(!enable);
        this.selectionComboBox.setDisable(!enable);
    }
}
