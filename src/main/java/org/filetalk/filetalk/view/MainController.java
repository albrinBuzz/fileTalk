package org.filetalk.filetalk.view;


import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.filetalk.filetalk.Client.Client;
import org.filetalk.filetalk.shared.Logger;

import java.io.IOException;

import static org.filetalk.filetalk.Tests.Gui.UserItem.createUserItem;

public class MainController {

    private Client client;
    private final MainView mainView;

    public MainController(MainView mainView, Client client) {
        this.mainView = mainView;
        this.client=client;
    }

    public void init(BorderPane root, Scene scene) {
        StringProperty currentTheme = new SimpleStringProperty("Claro");

        // Crea los distintos componentes de la vista
        HBox header = createHeader();
        HBox connectionStatus = createConnectionStatus();
        HBox topMenu = createTopMenu(scene);

        // Agrega los elementos al layout
        VBox centerBox = createCenterBox();

        VBox mainBox = new VBox();
        mainBox.getChildren().addAll(header, connectionStatus, topMenu);

        ScrollPane mainScroll = new ScrollPane(centerBox);
        mainScroll.setFitToWidth(true);

        root.setTop(mainBox);
        root.setCenter(mainScroll);
        root.setBottom(createStatusFooter());
    }

    public void startServer() {

        if (!mainView.isServerRunning()) {
            // Simulamos que el servidor se enciende
            mainView.getServerStatusLabel().setText("[Servidor: Iniciado]");
            mainView.getServerStatusLabel().setTextFill(Color.LIGHTGREEN);
            mainView.setServerRunning(true);
            mainView.getStartServerBtn().setText("Detener Servidor");
            Service<Void> servicio = new Service<Void>() {
                @Override
                protected Task<Void> createTask() {
                    return new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            try {
                                // Intentar iniciar el servidor

                                mainView.getServer().startServer();

                            }
                            catch (Exception e) {
                                // En caso de error, notificamos al usuario
                                System.out.println(e.getMessage());
                                /*Platform.runLater(() -> showAlert("Error al iniciar el servidor", e.getMessage()));
                                // Actualizamos el estado del servidor en la UI
                                Platform.runLater(() -> {
                                    isServerRunning = false;
                                    serverStatusCircle.setFill(Color.RED);  // Indicar que el servidor no está en marcha
                                    btnStartServer.setText("Iniciar Servidor");  // Restaurar el texto del botón
                                });*/
                            }
                            return null;
                        }
                    };
                }
            };

            servicio.start(); // Inicia el servicio

        } else {
            // Si el servidor ya está encendido, lo detenemos
            try {
               stopServer();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void stopServer() throws IOException {
        // Simulamos que el servidor se apaga


        try {
            mainView.getServer().stopServer();
        }catch (Exception e){

        }finally {
            // Cambiar el texto del botón a "Encender Servidor"
            mainView.getStartServerBtn().setText("Encender Servidor");
            mainView.getServerStatusLabel().setText("[Servidor: Detenido]");
            mainView.getServerStatusLabel().setTextFill(Color.RED);
            mainView.setServerRunning(false);

        }

    }

    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setPadding(new Insets(10));
        header.setStyle("-fx-background-color: #2c3e50;");

        Label userLabel = new Label("👤 Juan Ortega");
        userLabel.setTextFill(Color.WHITE);
        userLabel.setFont(Font.font(16));

        Label statusLabel = new Label("[🟢 Disponible ▼]");
        statusLabel.setTextFill(Color.LIGHTGREEN);
        statusLabel.setFont(Font.font(14));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button configBtn = new Button("⚙️ Configuración");
        Button securityBtn = new Button("🔒 Seguridad");
        Button logoutBtn = new Button("🚪 Cerrar sesión");

        header.getChildren().addAll(userLabel, statusLabel, spacer, configBtn, securityBtn, logoutBtn);

        return header;
    }

    public void connectServer(){

    }

    private HBox createConnectionStatus() {
        // Aquí se puede agregar el código relacionado con el estado de la conexión
        return new HBox(10);
    }

    private HBox createTopMenu(Scene scene) {
        HBox topMenu = new HBox(10);
        topMenu.setPadding(new Insets(10));
        topMenu.setAlignment(Pos.CENTER_LEFT);

        TextField searchUser = new TextField();
        searchUser.setPromptText("Buscar usuario...");
        searchUser.setPrefWidth(250);

        Button btnMisArchivos = new Button("📁 Mis archivos");
        Button btnEnviarArchivo = new Button("📤 Enviar archivo");
        Button btnTransferencias = new Button("📤 Transferencias");

        Region spacer3 = new Region();
        HBox.setHgrow(spacer3, Priority.ALWAYS);

        ComboBox<String> temaCombo = new ComboBox<>();
        temaCombo.getItems().addAll("Claro", "Oscuro");
        temaCombo.setValue("Claro");

        temaCombo.setOnAction(e -> {
            String tema = temaCombo.getValue();
            String temaPath = "/dark-theme.css";
            if ("Oscuro".equals(tema)) {
                scene.getStylesheets().clear();
                scene.getStylesheets().add(getClass().getResource(temaPath).toExternalForm());
            } else {
                temaPath = "/light-theme.css";
                scene.getStylesheets().clear();
                scene.getStylesheets().add(getClass().getResource(temaPath).toExternalForm());
            }
        });


        Label temaLabel = new Label("Tema:");
        Button personalizarLayout = new Button("⚙️ Personalizar Layout");
        ComboBox<String> idiomaCombo = new ComboBox<>();
        idiomaCombo.getItems().addAll("Español", "Inglés");
        idiomaCombo.setValue("Español");
        Label idiomaLabel = new Label("Idioma:");

        topMenu.getChildren().addAll(searchUser, btnMisArchivos, btnEnviarArchivo, btnTransferencias,
                spacer3, temaLabel, temaCombo, personalizarLayout, idiomaLabel, idiomaCombo);

        return topMenu;
    }

    private VBox createCenterBox() {
        VBox centerBox = new VBox(10);
        centerBox.setPadding(new Insets(5));
        centerBox.getChildren().addAll(createUserBox(), createSendFileBox());
        return centerBox;
    }

    private VBox createUserBox() {
        VBox usuariosBox = new VBox(5);
        usuariosBox.setPadding(new Insets(10));
        Label usuariosTitle = new Label("🧑‍💻 Usuarios Conectados (5)");
        usuariosTitle.setFont(Font.font(14));
        usuariosTitle.setStyle("-fx-font-weight: bold;");

        usuariosBox.getChildren().add(usuariosTitle);
        usuariosBox.getChildren().add(createUserItem("Ana Torres", "🟢 Online", "5 min ago", true));
        // Agregar más usuarios aquí...

        return usuariosBox;
    }

    private VBox createSendFileBox() {
        VBox envioRapidoBox = new VBox(5);
        envioRapidoBox.setPadding(new Insets(10));
        envioRapidoBox.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 1;");
        Label envioLabel = new Label("🚀 Envío rápido de archivo");
        envioLabel.setFont(Font.font(14));
        envioLabel.setStyle("-fx-font-weight: bold;");

        // Crear los diferentes componentes del envío rápido de archivo
        // Agregar los botones, campos de texto, etc.

        return envioRapidoBox;
    }

    private HBox createStatusFooter() {
        HBox estadoSistema = new HBox(15);
        estadoSistema.setPadding(new Insets(10));
        estadoSistema.setStyle("-fx-background-color: #ecf0f1;");

        Label usoLabel = new Label("📦 Uso: 5.9 GB / 6 GB");
        Label transHoyLabel = new Label("Transferencias hoy: 14");
        Label usuariosActLabel = new Label("Usuarios activos: 5/8");
        Label velRedLabel = new Label("📈 Velocidad red: 4.2 MB/s");
        Label cpuLabel = new Label("CPU: 35%");
        Label ramLabel = new Label("RAM: 62%");

        estadoSistema.getChildren().addAll(usoLabel, transHoyLabel, usuariosActLabel, velRedLabel, cpuLabel, ramLabel);

        return estadoSistema;
    }



    public void connectServer(String ip, String portStr) {




        Service<Void> servicio = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        try {
                            // Intentar iniciar el servidor


                            client.setConexion(ip, Integer.parseInt(portStr));
                            mainView.getConnectionStatusLabel().setText("[🟢 Conectado]");
                            mainView.getConnectionStatusLabel().setTextFill(Color.LIGHTGREEN);

                        }
                        catch (Exception e) {
                            // En caso de error, notificamos al usuario
                            Logger.logInfo(e.getMessage());
                                /*Platform.runLater(() -> showAlert("Error al iniciar el servidor", e.getMessage()));
                                // Actualizamos el estado del servidor en la UI
                                Platform.runLater(() -> {
                                    isServerRunning = false;
                                    serverStatusCircle.setFill(Color.RED);  // Indicar que el servidor no está en marcha
                                    btnStartServer.setText("Iniciar Servidor");  // Restaurar el texto del botón
                                });*/
                        }
                        return null;
                    }
                };
            }
        };

        servicio.start(); // Inicia el servicio

    }


}
