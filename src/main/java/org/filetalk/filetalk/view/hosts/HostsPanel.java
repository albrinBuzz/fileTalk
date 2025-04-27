package org.filetalk.filetalk.view.hosts;

import javafx.application.Platform;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import org.filetalk.filetalk.Client.ClientInfo;
import org.filetalk.filetalk.model.Observers.HostsObserver;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.filetalk.filetalk.Client.Client;
import org.filetalk.filetalk.shared.Logger;

import java.util.List;

public class HostsPanel extends ScrollPane implements HostsObserver {

    private VBox vbox; // Contenedor donde vamos a agregar los elementos (ej. etiquetas)
    private Pane pane;
    private Client cliente;

    public HostsPanel(Client cliente) {
        // Crear un VBox como contenedor para los servicios
        vbox = new VBox();
        vbox.setSpacing(10);  // Espacio entre los elementos
        vbox.setStyle("-fx-padding: 10; -fx-background-color: #2e2e2e;");  // Fondo oscuro con margen
        this.cliente = cliente;

        // Título de la sección de clientes conectados
        Label titleLabel = new Label("Clientes Conectados");
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #007bff; -fx-padding: 5px;");

        // Añadir el título al VBox al principio
        vbox.getChildren().add(titleLabel);

        // Establecer el VBox como el contenido del ScrollPane
        setContent(vbox);

        // Configurar el ScrollPane
        setFitToWidth(true);  // Ajustar el ancho del contenido
        setVbarPolicy(ScrollBarPolicy.ALWAYS); // Hacer visible la barra de desplazamiento vertical
        setHbarPolicy(ScrollBarPolicy.NEVER);  // No mostrar barra de desplazamiento horizontal
        this.setStyle("-fx-padding: 10; -fx-background-color: #2e2e2e;");  // Fondo oscuro con margen
        this.setStyle("-fx-padding: 20; -fx-background-color: #2d2d2d; -fx-border-color: #00BFFF; -fx-border-width: 2;"); // Celeste en los márgenes del root
    }

    @Override
    public void updateAllHosts(List<ClientInfo> hostList) {
        if (hostList == null || hostList.isEmpty()) {

            Platform.runLater(() -> vbox.getChildren().clear());
            return;  // Salir si la lista está vacía o nula
        }

        // Usar Platform.runLater para asegurarse de que el código se ejecute en el hilo de la interfaz de usuario
        Platform.runLater(() -> {
            // Limpiar el VBox antes de agregar los nuevos elementos
            vbox.getChildren().clear();

            // Añadir de nuevo el título
            Label titleLabel = new Label("Clientes Conectados");
            titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #007bff; -fx-padding: 5px;");
            vbox.getChildren().add(titleLabel);  // Reinsertar el título

            // Agregar un elemento por cada host en la lista
            for (ClientInfo host : hostList) {
                if (!host.getNick().equals("enviando")) {
                    HostControlPanel hostControlPanel = new HostControlPanel(host, this.cliente);
                    // Agregar el panel de host al VBox
                    hostControlPanel.setStyle("-fx-background-color: #2e2e2e;");  // Fondo oscuro para cada panel de host
                    vbox.getChildren().add(hostControlPanel);
                }
            }

            // Desplazarse automáticamente hacia abajo cuando se actualicen los elementos
            setVvalue(1.0);  // Mover la barra de desplazamiento hacia el final
        });
    }
}
