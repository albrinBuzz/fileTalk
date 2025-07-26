package org.filetalk.filetalk.Tests.Gui.hosts;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.filetalk.filetalk.Client.Client;
import org.filetalk.filetalk.Client.ClientInfo;
import org.filetalk.filetalk.model.Observers.HostsObserver;
import org.filetalk.filetalk.shared.Logger;


import java.util.List;

public class HostsPanel extends VBox implements HostsObserver {

    private Client cliente;
    private VBox usuariosBox;  // VBox donde se añaden los paneles de hosts
    private ScrollPane usuariosScroll;  // El ScrollPane que contendrá el VBox
    private Label usuariosTitle;

    public HostsPanel(Client cliente) {
        this.cliente = cliente;
        //cliente.addObserver(this);
        cliente.addHostOserver(this);

        // Crear un VBox para contener los paneles de los hosts
        usuariosBox = new VBox(5);
        usuariosBox.setSpacing(10);  // Espacio entre los elementos del VBox
        usuariosBox.setStyle("-fx-padding: 10; -fx-background-color: #2e2e2e;");  // Fondo oscuro con margen
        usuariosBox.setPadding(new Insets(10));

        usuariosTitle = new Label("🧑‍💻 Usuarios Conectados (5)");
        usuariosTitle.setFont(Font.font(14));
        usuariosTitle.setStyle("-fx-font-weight: bold;");

        usuariosBox.getChildren().add(usuariosTitle);

        // Crear un ScrollPane que contendrá el VBox
        usuariosScroll = new ScrollPane(usuariosBox);
        usuariosScroll.setFitToWidth(true);
        usuariosScroll.setPrefHeight(180);

        // Título de la sección de clientes conectados
        Label titleLabel = new Label("Clientes Conectados");
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #007bff; -fx-padding: 5px;");

        // Añadir el título al VBox al principio
        //usuariosBox.getChildren().add(titleLabel);  // Título añadido al VBox

        // Establecer el VBox como contenido del ScrollPane
        //usuariosScroll.setContent(usuariosBox);

        // Configuración del ScrollPane
        usuariosScroll.setFitToWidth(true);  // Ajustar el ancho del contenido
        /*usuariosScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);  // Siempre mostrar la barra de desplazamiento vertical
        usuariosScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);  // Nunca mostrar barra de desplazamiento horizontal
        usuariosScroll.setVvalue(1.0);  // Asegurarse de que la barra de desplazamiento esté al final

        // Asegurarse de que el ScrollPane ocupe todo el espacio disponible
        usuariosScroll.setMaxHeight(Double.MAX_VALUE);
        usuariosScroll.setFitToHeight(true);*/

        // Añadir el ScrollPane al HostsPanel
        this.getChildren().add(titleLabel);  // HostsPanel ahora contiene el ScrollPane

        this.getChildren().add(usuariosScroll);  // HostsPanel ahora contiene el ScrollPane

        // Estilo del HostsPanel
        this.setStyle("-fx-padding: 20; -fx-background-color: #2d2d2d; -fx-border-color: #00BFFF; -fx-border-width: 2;"); // Bordes azules
    }

    @Override
    public void updateAllHosts(List<ClientInfo> hostList) {
        Logger.logInfo("actualizado la lista de host");
        if (hostList == null || hostList.isEmpty()) {
            Logger.logInfo("la lista de host esta vacia ");

            Platform.runLater(() -> this.getChildren().clear());
            return;  // Si la lista está vacía o nula, no hacer nada
        }
        hostList.forEach(clientInfo -> Logger.logInfo(clientInfo.getNick()));

        // Usar Platform.runLater para asegurar que las actualizaciones se realicen en el hilo de la interfaz de usuario
        Platform.runLater(() -> {
            // Limpiar el VBox antes de agregar los nuevos elementos
            usuariosBox.getChildren().clear();

            Label usuariosTitle = new Label("🧑‍💻 Usuarios Conectados (" + hostList.size() + ")");
            usuariosTitle.setFont(Font.font(14));
            usuariosTitle.setStyle("-fx-font-weight: bold;");
            usuariosBox.getChildren().add(usuariosTitle);

            // Añadir los usuarios a la lista
            /*for (ClientInfo usuario : hostList) {
                usuariosBox.getChildren().add(createUserItem(usuario.getNick()));
            }*/



            // Agregar un elemento por cada host en la lista
            for (ClientInfo host : hostList) {
                if (!host.getNick().equals("enviando")||!host.getNick().chars().allMatch(Character::isDigit)) {
                    HostControlPanel hostControlPanel = new HostControlPanel(host, this.cliente);
                    hostControlPanel.setStyle("-fx-background-color: #2e2e2e;");  // Fondo oscuro para cada panel de host
                    usuariosBox.getChildren().add(hostControlPanel);  // Agregar el panel del host al VBox
                }
            }

            // Desplazarse automáticamente hacia abajo cuando se actualicen los elementos
            usuariosScroll.setVvalue(1.0);  // Mover la barra de desplazamiento hacia el final
        });
    }

    private VBox createUserItem(String username) {
        VBox userBox = new VBox(5);
        userBox.setPadding(new Insets(5));

        // Puedes personalizar estos detalles (status, tiempo, etc.) según tu necesidad
        Label usernameLabel = new Label(username);
        usernameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ffffff;");
        userBox.getChildren().add(usernameLabel);

        // Aquí se podría agregar un label para mostrar el estado de conexión, tiempo, etc.
        Label statusLabel = new Label("🟢 Online");  // Ejemplo de estado
        statusLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #888;");
        userBox.getChildren().add(statusLabel);

        return userBox;
    }

    private HBox createUserItem(String nombre, String estado, String ultimaConexion, boolean mostrarArchivos) {
        HBox userItem = new HBox(10);
        userItem.setPadding(new Insets(5));
        userItem.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 0 0 1 0;");
        userItem.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label(estado);
        Label nameLabel = new Label("👤 " + nombre);
        Label lastConn = new Label("Última conexión: " + ultimaConexion);
        lastConn.setStyle("-fx-font-style: italic;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button enviarArchivoBtn = new Button("📤 Enviar archivo");
        userItem.getChildren().addAll(nameLabel, icon, lastConn, spacer, enviarArchivoBtn);

        if (mostrarArchivos) {
            Button archivosBtn = new Button("📁 Archivos");
            userItem.getChildren().add(archivosBtn);
        }

        return userItem;
    }

}
