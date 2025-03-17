package org.filetalk.filetalk;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.filetalk.filetalk.model.Observers.Observer;
import org.filetalk.filetalk.Client.Client;
import org.filetalk.filetalk.view.hosts.HostsPanel;
import org.filetalk.filetalk.view.servidor.ServerInfoPane;
import org.filetalk.filetalk.view.tranferens.TransferencesPanel;


import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
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
    public Pane serverPane;
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
    private Button sendButton;
    private Button cancelButton;
    @FXML
    private Button chooseFileButton;
    @FXML
    private Button chooseFolderButton;
    private Button clearHistoryButton;
    @FXML
    private Button searchServerButton;
    @FXML
    private Button connectButton;
    private Button viewHistoryButton;  // Nuevo botón para ver el historial
    private Button downloadHistoryButton;  // Nuevo botón para descargar el historial
    private ListView<String> transferHistory;
    @FXML
    private ListView<ProgressBar> receivingFilesList;
    private List<File> filesToSend = new ArrayList<>();
    private volatile boolean cancelTransfer = false;
    private String serverIP;
    private int serverPort;
    private ListView<String> clientsListView;
    private AtomicBoolean isReceiving = new AtomicBoolean(false); // Para controlar la recepción de archivos
    
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

    private Socket socket;
    private DataOutputStream dataOutputStream;
    private static final String SERVER_ADDRESS = "192.168.100.5";
    private static final int SERVER_PORT = 9090;
    private Client client;
    private HostsPanel hostsPanel;
    @FXML
    void enviarMensaje(ActionEvent event) throws IOException {
        String message = txtMensajes.getText();
        //PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        //writer.println(message);
        client.enviarMensaje(message);
        appendMessage("Yo: " + message+"\n", true); // Mensaje enviado

        txtMensajes.clear(); // Limpiar el campo de texto después de enviar el mensaje
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //ipComboBox.setPromptText("Select Server IP");

// Agregar algunas direcciones IP de prueba
        ipComboBox.getItems().addAll(
                "192.168.1.10",
                "192.168.100.5",
                "10.220.57.99",
                "192.168.1.13"
        );

        client = new Client();


        // Establecer una opción predeterminada
        ipComboBox.setValue("192.168.100.5");
        portField.setText("9090");

        // Agregar un listener para manejar la selección
        ipComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("IP seleccionada: " + newValue);
        });
        vboxMsj.setPadding(new Insets(10));
        vboxMsj.setSpacing(5);
        ipComboBox.valueProperty().addListener((obs, oldValue, newValue) -> validateConnectionFields(ipComboBox, portField));
        portField.textProperty().addListener((obs, oldValue, newValue) -> validateConnectionFields(ipComboBox, portField));

        // Agregar un listener para manejar la selección
        /*ipComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("IP seleccionada: " + newValue);
        });*/


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

        TransferencesPanel panelTransfers=new TransferencesPanel();

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
        //cbClientes.getItems().addAll("Opción 1", "Opción 2", "Opción 3", "Opción 4");



    }

    private void validateConnectionFields(ChoiceBox<String> ipComboBox, TextField portField) {
        String ip = ipComboBox.getValue();
        String port = portField.getText();

        // Validar IP y puerto
        if (ip == null || ip.isEmpty() || port.isEmpty()) {
            connectButton.setDisable(true);
            statusText.setText("Please enter both Server IP and Port.");
        } else {
            connectButton.setDisable(false);
            statusText.setText("Ready to connect...");
        }
    }

    private void setMesajes() {



            while (true) {
                //System.out.println("leyendo el mensaje");
                if (client.getMsj()!=null){
                    //System.out.println(Thread.currentThread());
                    //System.out.println(client.getMsj());
                    appendMessage(client.getMsj(),false);
                    client.resetMsj();
                }



                try {
                    Thread.sleep(5); // Actualiza cada 5 segundos
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                System.gc();
            }


        /*Task<Void> leerMensajes = new Task<>() {
            @Override
            protected Void call() {
                Platform.runLater(()->{
                while (true){
                    //System.out.println("leyendo el mensaje");
                    if (client.getMsj()!=null){

                        appendMessage(client.getMsj(),false);
                    }
                }
                });
                return null;
            }
        };
        new Thread(leerMensajes).start();*/
    }

    private void appendMessage(String message, boolean isSent) {

            Platform.runLater(()->{
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

    /*private void connectToServer() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            //txtConectado.setText("Cliente: " + socket.getInetAddress().getHostAddress());
        } catch (IOException e) {
            showAlert("Error", "No se pudo conectar al servidor.");
            e.printStackTrace();
        }
    }*/

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void enviarArchivo(ActionEvent actionEvent) throws IOException {
        // Crear el diálogo
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Enviar Archivo");
        dialog.setHeaderText("Selecciona un archivo y un destinatario.");
        dialog.setResizable(false);

        // Establecer un tamaño específico
        dialog.setWidth(400);
        dialog.setHeight(200);

        // Crear componentes
        GridPane grid = new GridPane();
        TextField fileField = new TextField();
        //TextField recipientField = new TextField();
        ComboBox<String>ipReceiver=receiverComboBox;
        ipReceiver.setValue("seleccione un destinatario");

        Button fileButton = new Button("Seleccionar Archivo");

        // Configurar el FileChooser
        FileChooser fileChooser = new FileChooser();

        // Acción para seleccionar un archivo
        fileButton.setOnAction(e -> {
            File file = fileChooser.showOpenDialog(vboxMain.getScene().getWindow());
            if (file != null) {
                fileField.setText(file.getAbsolutePath());
            }
        });

        // Agregar los componentes al grid
        grid.add(new Label("Archivo:"), 0, 0);
        grid.add(fileField, 1, 0);
        grid.add(fileButton, 2, 0);
        grid.add(new Label("Destinatario:"), 0, 1);
        grid.add(ipReceiver, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Añadir botones
        ButtonType sendButtonType = new ButtonType("Enviar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(sendButtonType, ButtonType.CANCEL);

        // Manejar la acción de envío
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == sendButtonType) {
                String filePath = fileField.getText();
                String recipient = ipReceiver.getValue();
                String serverAddress = ipComboBox.getValue(); // Dirección IP del servidor
                int serverPort = Integer.parseInt(portField.getText()); // Puerto

                // Verificar si los campos no están vacíos
                if (filePath.isEmpty() || recipient.isEmpty()) {
                    showAlert("Error", "Por favor, ingresa todos los campos.");
                    return null;
                }

                // Crear una nueva tarea en segundo plano para enviar el archivo
                File file = new File(filePath);
                Task<Void> task = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        // Aquí se realiza el envío del archivo en un hilo separado
                        //client.setConexion(serverAddress, serverPort); // Establecer conexión
                        //client.enviarMensaje(recipient); // Enviar mensaje al destinatario
                        //client.sendFile(file, "/file " + recipient + " " + filePath, serverAddress);
                        //client.handleFileTransfer("/file " + recipient + " " + filePath);


                        return null;
                    }
                };

                // Manejar progreso (si es necesario)
                task.setOnSucceeded(e1 -> {
                    System.out.println("Archivo enviado correctamente.");
                    appendMessage("Archivo enviado a " + recipient, true);
                });

                task.setOnFailed(e1 -> {
                    System.out.println("Error al enviar el archivo: " + task.getException().getMessage());
                    appendMessage("Error al enviar el archivo: " + task.getException().getMessage(), true);
                });
                Task<Void> task2 = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        ProgressBar fileProgressBar = new ProgressBar(0);
                        Platform.runLater(() -> receivingFilesList.getItems().add(fileProgressBar));

                        int totalChunks = 100;
                        Random random = new Random();

                        // Simula la recepción del archivo en partes
                        for (int i = 0; i <= totalChunks; i++) {
                            if (cancelTransfer) {
                                break;
                            }

                            try {
                                Thread.sleep(random.nextInt(100)); // Simular un tiempo aleatorio para cada parte
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            final int progress = i;
                            Platform.runLater(() -> fileProgressBar.setProgress(progress / (double) totalChunks));
                        }

                        // Al finalizar la recepción del archivo, actualizar el historial
                        Platform.runLater(() -> {
                            // Actualizar el historial de transferencias con el archivo recibido
                            transferHistory.getItems().add("Received: " + file.getName());
                            //receivingFilesList.getItems().remove(fileProgressBar); // Eliminar el ProgressBar del archivo recibido
                        });

                        return null;
                    }
                };

                // Manejar progreso (si es necesario)
                task.setOnSucceeded(e1 -> {
                    System.out.println("Archivo enviado correctamente.");
                    appendMessage("Archivo enviado a " + recipient, true);
                });

                task.setOnFailed(e1 -> {
                    System.out.println("Error al enviar el archivo: " + task.getException().getMessage());
                    appendMessage("Error al enviar el archivo: " + task.getException().getMessage(), true);
                });



                // Ejecutar la tarea en un hilo separado
                new Thread(task).start();

                new Thread(task2).start();



            }
            return null;
        });




        // Mostrar el diálogo
        dialog.showAndWait();




        //client.sendFile(file, "/file jose " + filePath, serverAddress);
        //client.handleFileTransfer("/file jose /home/cris/Descargas/sqlbrowserfx-fat.jar");


    }


    private void validateConnectionFields(ComboBox<String> ipComboBox, TextField portField) {
        String ip = ipComboBox.getValue();
        String port = portField.getText();

        // Validar IP y puerto
        if (ip == null || ip.isEmpty() || port.isEmpty()) {
            connectButton.setDisable(true);
            statusText.setText("Please enter both Server IP and Port.");
        } else {
            connectButton.setDisable(false);
            statusText.setText("Ready to connect...");
        }
    }



    // Método para enviar el mensaje del chat
    private void sendMessage() {
        String message = inputField.getText();
        if (!message.isEmpty()) {
            addMessage("Yo: " + message, true);
            inputField.clear();
            // Simulación de un mensaje recibido
            addMessage("Usuario2: Respuesta simulada", false);
        }
    }

    private void sendFile() {
        if (filesToSend.isEmpty()) {
            statusText.setText("No file selected.");
            return;
        }

        if (serverIP == null || serverIP.isEmpty()) {
            statusText.setText("Please enter a valid server IP.");
            return;
        }

        statusText.setText("Sending file...");
        cancelButton.setDisable(false);
        transferHistory.getItems().add("Started sending files...");
        progressBar.setProgress(0);
        cancelTransfer = false;

        // Create a separate progress bar for each file being transferred
        List<ProgressBar> fileProgressBars = new ArrayList<>();
        for (int i = 0; i < filesToSend.size(); i++) {
            ProgressBar fileProgress = new ProgressBar(0);
            fileProgress.setPrefWidth(300);
            fileProgressBars.add(fileProgress);
            receivingFilesList.getItems().add(fileProgress);
        }

        Thread sendThread = new Thread(() -> {
            try {
                for (int i = 0; i < filesToSend.size(); i++) {
                    if (cancelTransfer) {
                        break;
                    }
                    File file = filesToSend.get(i);
                    transferHistory.getItems().add("Sending: " + file.getName());
                    progressBar.setProgress((i + 1) / (double) filesToSend.size());

                    // Simulate file transfer by sleeping for a period of time
                    simulateFileTransfer(file, fileProgressBars.get(i));

                    transferHistory.getItems().add("Finished sending: " + file.getName());
                }
                statusText.setText("File transfer completed.");
            } catch (Exception e) {
                statusText.setText("Error during transfer: " + e.getMessage());
            } finally {
                cancelButton.setDisable(true);
            }
        });
        sendThread.start();
    }

    private void simulateFileTransfer(File file, ProgressBar progressBar) {
        try {
            int totalChunks = 100;
            for (int i = 0; i <= totalChunks; i++) {
                if (cancelTransfer) {
                    break;
                }
                Thread.sleep(50); // Simulate transfer delay
                final int progress = i;
                Platform.runLater(() -> progressBar.setProgress(progress / (double) totalChunks));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void chooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files", "*.*"));
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {

            filePathField.setText(file.getAbsolutePath());  // Actualiza el TextField con el path del archivo seleccionado

            filesToSend.clear();
            filesToSend.add(file); // agrega el archivo a la lista para enviarlo
            sendButton.setDisable(false);
        }
    }

    private void chooseFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File folder = directoryChooser.showDialog(null);
        if (folder != null) {
            filePathField.setText(folder.getAbsolutePath());  // Actualiza el TextField con el path de la carpeta seleccionada
            filesToSend.clear();
            addFilesFromFolder(folder); // agrega todos los archivos de la carpeta seleccionada
            sendButton.setDisable(false);
        }
    }

    private void addFilesFromFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    filesToSend.add(file);
                }
            }
        }
    }

    // Agregar un mensaje al chat
    private void addMessage(String message, boolean isSent) {
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

        messagesBox.getChildren().add(messageContainer);
        messagesBox.heightProperty().addListener((obs, oldVal, newVal) -> {
            scrollPane.setVvalue(1.0); // Desplazar hacia abajo al agregar un nuevo mensaje
        });
    }

    // Simulación de la conexión al servidor
    @FXML
    private void searchServer() {
        //statusText.setText("Searching for server...");
        //statusText.setText("Servers found. Select one.");
        System.out.println("buscando");

        try {
            client.conexionAutomatica();
            connectButton.setDisable(true);
            searchServerButton.setDisable(true);

            //lblStatusConexion.setText("Conectado");
            lblConexion.setText("Conectado");



            //Thread thread= new Thread(this::setMesajes);
            //Platform.runLater(()->thread.start());

        } catch (IOException e) {
            showAlert("cago",e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    @FXML
    private void connectToServer() throws IOException {
        String ip = (String) ipComboBox.getValue();
        String portStr = portField.getText();



        // Validación adicional antes de intentar la conexión
        if (ip == null || ip.isEmpty() || portStr.isEmpty()) {
            statusText.setText("Please enter both Server IP and Port.");
            return;
        }

        try {
            serverIP = ip;
            serverPort = Integer.parseInt(portStr);

            try {
                client.setConexion(serverIP,serverPort);
                connectionStatus.setText("Conexión Establecida");
                connectButton.setDisable(true);
                searchServerButton.setDisable(true);
                //client.setConexion("192.168.100.5",9090);
                //client.addObserver(hostsPanel);

                //Thread thread= new Thread(this::setMesajes);
                //Platform.runLater(()->thread.start());


            } catch (IOException e) {
                showAlert("cago",e.getMessage());
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            //setMesajes();

            // Intentar conexión
            //statusText.setText("Connecting to " + serverIP + ":" + serverPort + "...");
            // Simulamos la conexión aquí


            //statusText.setText("Connected to " + serverIP + ":" + serverPort);
            connectButton.setDisable(true); // Desactivar el botón después de conectar

            // Inicia la simulación de recibir archivos
            //simulateFileReception();
        } catch (NumberFormatException e) {
            statusText.setText("Invalid port format.");
        }
    }




    // Lógica para recibir archivos simulados en paralelo
    private void simulateFileReception() {
        if (isReceiving.get()) {
            return;
        }

        isReceiving.set(true);
        //cancelButton.setDisable(false);
        statusText.setText("Receiving files...");

        // Simulación de archivos que llegan del "servidor"
        List<File> incomingFiles = new ArrayList<>();
        incomingFiles.add(new File("documento1.txt"));
        incomingFiles.add(new File("imagen.png"));
        incomingFiles.add(new File("video.mp4"));

        // Simulación de transferencia de archivos entrantes en paralelo
        new Thread(() -> {
            List<Thread> threads = new ArrayList<>();
            for (File file : incomingFiles) {
                Thread fileThread = new Thread(() -> simulateFileReception(file));
                fileThread.start();
                threads.add(fileThread);
            }

            // Esperar a que todos los hilos terminen
            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Platform.runLater(() -> {
                statusText.setText("File reception completed.");
                cancelButton.setDisable(true);
                isReceiving.set(false);
            });
        }).start();
    }

    // Simula la recepción de un archivo
    private void simulateFileReception(File file) {
        ProgressBar fileProgressBar = new ProgressBar(0);
        Platform.runLater(() -> receivingFilesList.getItems().add(fileProgressBar));

        int totalChunks = 100;
        Random random = new Random();

        // Simula la recepción del archivo en partes
        for (int i = 0; i <= totalChunks; i++) {
            if (cancelTransfer) {
                break;
            }

            try {
                Thread.sleep(random.nextInt(100)); // Simular un tiempo aleatorio para cada parte
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            final int progress = i;
            Platform.runLater(() -> fileProgressBar.setProgress(progress / (double) totalChunks));
        }

        // Al finalizar la recepción del archivo, actualizar el historial
        Platform.runLater(() -> {
            // Actualizar el historial de transferencias con el archivo recibido
            transferHistory.getItems().add("Received: " + file.getName());
            receivingFilesList.getItems().remove(fileProgressBar); // Eliminar el ProgressBar del archivo recibido
        });
    }

    // Cancela la recepción de archivos
    private void cancelTransfer() {
        cancelTransfer = true;
        statusText.setText("Transfer canceled.");
        progressBar.setProgress(0);
        cancelButton.setDisable(true);
    }

    private void clearHistory() {
        transferHistory.getItems().clear();
        statusText.setText("Ready to receive files...");
        progressBar.setProgress(0);
    }

    // Nueva funcionalidad para ver el historial de chat
    private void viewHistory() {
        StringBuilder history = new StringBuilder();
        for (int i = 0; i < messagesBox.getChildren().size(); i++) {
            HBox messageContainer = (HBox) messagesBox.getChildren().get(i);
            TextArea messageArea = (TextArea) messageContainer.getChildren().get(0);
            history.append(messageArea.getText()).append("\n");
        }

        TextArea historyArea = new TextArea(history.toString());
        historyArea.setEditable(false);
        historyArea.setWrapText(true);

        Stage historyStage = new Stage();
        historyStage.setTitle("Chat History");
        historyStage.setScene(new Scene(historyArea, 600, 400));
        historyStage.show();
    }

    // Nueva funcionalidad para descargar el historial de chat
    private void downloadHistory() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (int i = 0; i < messagesBox.getChildren().size(); i++) {
                    HBox messageContainer = (HBox) messagesBox.getChildren().get(i);
                    TextArea messageArea = (TextArea) messageContainer.getChildren().get(0);
                    writer.write(messageArea.getText());
                    writer.newLine();
                }
                statusText.setText("History saved to: " + file.getAbsolutePath());
            } catch (IOException e) {
                statusText.setText("Error saving history.");
            }
        }
    }

    @Override
    public void update(String message) {

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
}
