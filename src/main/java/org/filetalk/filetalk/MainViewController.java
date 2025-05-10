package org.filetalk.filetalk;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import org.filetalk.filetalk.Client.Client;
import org.filetalk.filetalk.Client.ConfiguracionCliente;
import org.filetalk.filetalk.Client.UtilidadesCliente;
import org.filetalk.filetalk.model.Observers.Observer;
import org.filetalk.filetalk.shared.ServerStatusConnection;
import org.filetalk.filetalk.view.hosts.HostsPanel;
import org.filetalk.filetalk.view.servidor.ServerInfoPane;
import org.filetalk.filetalk.view.tranferens.TransferencesPanel;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainViewController implements Initializable, Observer {

    public ComboBox<String> receiverComboBox;
    public TextField ipReceptorTxt;
    public ListView<ProgressBar> sendigFilesList;
    public Pane hostPane;
    public Label connectionStatus;
    public Label lblStatusConexion;
    public Pane transfersPane;
    public Label lblConexion;
    public VBox serverPane;
    public Circle crConexion;
    public TextArea txtIpServidor;
    public Button btnDesconectarse;
    @FXML
    private ChoiceBox<String> ipComboBox;
    private VBox messagesBox;
    private TextField inputField;
    private ScrollPane scrollPane;
    @FXML
    private TextField filePathField;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Text statusText;
    @FXML
    private Button searchServerButton;
    @FXML
    private Button connectButton;

    @FXML
    private ListView<ProgressBar> receivingFilesList;
    private final List<File> filesToSend = new ArrayList<>();
    private final boolean cancelTransfer = false;
    private String serverIP;
    private int serverPort;
    private ListView<String> clientsListView;
    private final AtomicBoolean isReceiving = new AtomicBoolean(false); // Para controlar la recepción de archivos

    @FXML
    private TextField portField;
    public ScrollPane scrollMsj;
    public VBox vboxMain;
    public VBox vboxMsj;
    public ChoiceBox cbClientes;
    public Button btnEnviarArchivo;

    @FXML
    private Button btnEnvirMensaje;

    @FXML
    private TextField txtConectado;

    @FXML
    private TextField txtMensaje;

    @FXML
    private TextArea txtMensajes;

    private Client client;
    private HostsPanel hostsPanel;

    @FXML
    void enviarMensaje(ActionEvent event) throws IOException {
        String message = txtMensajes.getText();
        //PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        //writer.println(message);
        client.enviarMensaje(message);
        appendMessage("Yo: " + message + "\n", true); // Mensaje enviado

        txtMensajes.clear(); // Limpiar el campo de texto después de enviar el mensaje
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        crConexion.setFill(Color.RED);
        client = new Client();
        btnDesconectarse.setDisable(true);
        //ipReceptorTxt.setPromptText("192.168.100.5");
        //portField.setPromptText("8080");

        vboxMsj.setPadding(new Insets(10));
        vboxMsj.setSpacing(5);

        AnchorPane.setTopAnchor(vboxMain, 0.0);
        AnchorPane.setLeftAnchor(vboxMain, 0.0);
        AnchorPane.setRightAnchor(vboxMain, 0.0);

        portField.textProperty().addListener((obs, oldValue, newValue) -> validateConnectionFields(ipReceptorTxt, portField));


        // Crear el HostsPanel
        hostsPanel = new HostsPanel(client);

        // Establecer un tamaño inicial por defecto o inicializar después de que el Pane sea renderizado
        hostPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            // Asignar el nuevo ancho y alto del hostPane a HostsPanel
            hostsPanel.setPrefWidth(newValue.doubleValue());  // Ajusta el ancho de HostsPanel
        });

        hostPane.heightProperty().addListener((observable, oldValue, newValue) -> {
            // Asignar el nuevo alto del hostPane a HostsPanel
            hostsPanel.setPrefHeight(newValue.doubleValue());  // Ajusta el alto de HostsPanel
        });


        // Añadir el HostsPanel al Pane
        hostPane.getChildren().add(hostsPanel);

        TransferencesPanel panelTransfers = new TransferencesPanel();

        transfersPane.widthProperty().addListener((observable, oldValue, newValue) -> {

            panelTransfers.setPrefWidth(newValue.doubleValue());
        });

        transfersPane.heightProperty().addListener((observable, oldValue, newValue) -> {
            panelTransfers.setPrefHeight(newValue.doubleValue());
        });


        transfersPane.getChildren().add(panelTransfers);

        try {
            ServerInfoPane serverInfoPane = new ServerInfoPane();

            serverPane.widthProperty().addListener((observable, oldValue, newValue) -> {

                serverInfoPane.setPrefWidth(newValue.doubleValue());
            });

            serverPane.heightProperty().addListener((observable, oldValue, newValue) -> {
                serverInfoPane.setPrefHeight(newValue.doubleValue());
            });

            serverPane.getChildren().add(serverInfoPane);

        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }


        client.addObserver(this);
        client.addHostOserver(hostsPanel);
        client.setTransferencesObserver(panelTransfers);

        scrollMsj.setFitToWidth(true);
        scrollMsj.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS); // Siempre mostrar la barra vertical

    }

    private void validateConnectionFields(TextField ipReceptorTxt, TextField portField) {
        String ip = ipReceptorTxt.getText();
        String port = portField.getText();

        // Validar IP y puerto
        //statusText.setText("Please enter both Server IP and Port.");
        //statusText.setText("Ready to connect...");
        connectButton.setDisable(ip == null || ip.isEmpty() || port.isEmpty());
    }


    private void appendMessage(String message, boolean isSent) {

        Platform.runLater(() -> {
            TextArea messageArea = new TextArea(message);
            messageArea.setEditable(false);
            messageArea.setWrapText(true);
            messageArea.setPrefWidth(350); // Ajustar el ancho

            // Contenedor para el mensaje
            HBox messageContainer = new HBox();
            messageContainer.setPadding(new Insets(5));

            if (isSent) {
                messageArea.setStyle("-fx-background-color: lightblue; -fx-alignment: center-right;");
                messageContainer.getChildren().add(messageArea);
                messageContainer.setStyle("-fx-alignment: center-right;"); // Alinea a la derecha
            } else {
                messageArea.setStyle("-fx-background-color: lightgray; -fx-alignment: center-left;");
                messageContainer.getChildren().add(messageArea);
                messageContainer.setStyle("-fx-alignment: center-left;"); // Alinea a la izquierda
            }

            vboxMsj.getChildren().add(messageContainer);
            vboxMsj.heightProperty().addListener((obs, oldVal, newVal) -> {
                scrollMsj.setVvalue(1.0); // Desplazar hacia abajo al agregar un nuevo mensaje
            });
        });


    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }


    // Simulación de la conexión al servidor
    @FXML
    private void searchServer() {
        //statusText.setText("Searching for server...");
        //statusText.setText("Servers found. Select one.");
        //System.out.println("buscando");
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Callable<Void> task = () -> {
            client.conexionAutomatica();
            Platform.runLater(() -> {
                connectButton.setDisable(true);
                searchServerButton.setDisable(true);
                lblConexion.setText("Conectado");
                crConexion.setFill(Color.GREEN);

                btnDesconectarse.setDisable(false);
            });
            return null;
        };

        try {

            executor.invokeAny(java.util.Collections.singleton(task), 5, TimeUnit.SECONDS);

        } catch (TimeoutException e) {

            client.cerrarBusqueda();
            Platform.runLater(() -> showAlert("Tiempo Excedido", "No se encontró el servidor"));

        } catch (InterruptedException | ExecutionException e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }

    @FXML
    private void connectToServer() {
        String ip = ipReceptorTxt.getText();
        String portStr = portField.getText();

        // Validación adicional antes de intentar la conexión
        if (ip == null || ip.isEmpty() || portStr.isEmpty()) {
            showAlert("Campos Imcompletos", "Ingrese la ip y Puerto del servidor");
            return;
        }

        try {
            serverIP = ip;
            serverPort = Integer.parseInt(portStr);

            try {
                client.setConexion(serverIP, serverPort);
                //connectionStatus.setText("Conexión Establecida");
                connectButton.setDisable(true);
                searchServerButton.setDisable(true);
                btnDesconectarse.setDisable(false);

            } catch (IOException e) {
                showAlert("cago", e.getMessage());
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            connectButton.setDisable(true); // Desactivar el botón después de conectar


        } catch (NumberFormatException e) {
            statusText.setText("Invalid port format.");
        }
    }


    @Override
    public void updateServerConnection(ServerStatusConnection statusConnection) {

        if (statusConnection.equals(ServerStatusConnection.DISCONNECTED)) {
            Platform.runLater(() -> {
                connectButton.setDisable(false);
                searchServerButton.setDisable(false);
                lblConexion.setText("Desconectado");
                crConexion.setFill(Color.RED);

                btnDesconectarse.setDisable(true);
            });

        }
    }

    @Override
    public void updateClientsList(List<String> clients) {
        Platform.runLater(() -> {
            ObservableList<String> observableList = FXCollections.observableArrayList(clients);

            receiverComboBox.getItems().clear();

            receiverComboBox.getItems().addAll(clients);  // Actualiza la lista de clientes

        });
    }

    @Override
    public void updateMessaje(String message) {
        Platform.runLater(() -> {
            appendMessage(message, false);  // Llama a la función para agregar el mensaje
        });
    }


    public void chooseMultipleFiles(ActionEvent actionEvent) {
    }

    public void openConfiguration(ActionEvent actionEvent) {
    }

    public void openAdvancedSettings(ActionEvent actionEvent) {
    }

    public void toggleAutoConnect(ActionEvent actionEvent) {
    }

    public void startServer(ActionEvent actionEvent) {
    }

    public void stopServer(ActionEvent actionEvent) {
    }

    public void restartServer(ActionEvent actionEvent) {
    }

    public void viewServerLogs(ActionEvent actionEvent) {
    }

    public void openServerConfig(ActionEvent actionEvent) {
    }

    public void desconctarServidor(ActionEvent actionEvent) {
        client.desconect();
        connectButton.setDisable(false);
        searchServerButton.setDisable(false);
        btnDesconectarse.setDisable(true);
    }

    public void abrirDescargas(ActionEvent actionEvent) {
        ConfiguracionCliente config = new ConfiguracionCliente();
        String rutaDescargas = config.obtener("cliente.directorio_descargas");

        new Thread(() -> {
            boolean exito = UtilidadesCliente.abrirDirectorioDescargas(rutaDescargas);
            if (!exito) {
                Platform.runLater(() -> {
                    // Mostrar un Alert si falla, desde el hilo de la UI
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("No se pudo abrir la carpeta de descargas.");
                    alert.setContentText("Verifica que la carpeta exista y tengas permisos.");
                    alert.showAndWait();
                });
            }
        }).start();


    }
}
