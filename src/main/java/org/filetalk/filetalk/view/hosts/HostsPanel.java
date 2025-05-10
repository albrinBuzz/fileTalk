package org.filetalk.filetalk.view.hosts;

import javafx.application.Platform;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import org.filetalk.filetalk.Client.Client;
import org.filetalk.filetalk.Client.ClientInfo;
import org.filetalk.filetalk.model.Observers.HostsObserver;

import java.util.List;

public class HostsPanel extends VBox implements HostsObserver {

    private Client cliente;
    private VBox vbox;  // VBox donde se añaden los paneles de hosts
    private ScrollPane scroll;  // El ScrollPane que contendrá el VBox

    public HostsPanel(Client cliente) {
        this.cliente = cliente;

        // Crear un VBox para contener los paneles de los hosts
        vbox = new VBox();
        vbox.setSpacing(10);  // Espacio entre los elementos del VBox
        vbox.setStyle("-fx-padding: 10; -fx-background-color: #2e2e2e;");  // Fondo oscuro con margen

        // Crear un ScrollPane que contendrá el VBox
        scroll = new ScrollPane();

        // Título de la sección de clientes conectados
        Label titleLabel = new Label("Clientes Conectados");
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #007bff; -fx-padding: 5px;");

        // Añadir el título al VBox al principio
        //vbox.getChildren().add(titleLabel);  // Título añadido al VBox

        // Establecer el VBox como contenido del ScrollPane
        scroll.setContent(vbox);

        // Configuración del ScrollPane
        scroll.setFitToWidth(true);  // Ajustar el ancho del contenido
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);  // Siempre mostrar la barra de desplazamiento vertical
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);  // Nunca mostrar barra de desplazamiento horizontal
        scroll.setVvalue(1.0);  // Asegurarse de que la barra de desplazamiento esté al final

        // Asegurarse de que el ScrollPane ocupe todo el espacio disponible
        scroll.setMaxHeight(Double.MAX_VALUE);
        scroll.setFitToHeight(true);

        // Añadir el ScrollPane al HostsPanel
        this.getChildren().add(titleLabel);  // HostsPanel ahora contiene el ScrollPane

        this.getChildren().add(scroll);  // HostsPanel ahora contiene el ScrollPane

        // Estilo del HostsPanel
        this.setStyle("-fx-padding: 20; -fx-background-color: #2d2d2d; -fx-border-color: #00BFFF; -fx-border-width: 2;"); // Bordes azules
    }

    @Override
    public void updateAllHosts(List<ClientInfo> hostList) {
        if (hostList == null || hostList.isEmpty()) {
            Platform.runLater(() -> this.getChildren().clear());
            return;  // Si la lista está vacía o nula, no hacer nada
        }

        // Usar Platform.runLater para asegurar que las actualizaciones se realicen en el hilo de la interfaz de usuario
        Platform.runLater(() -> {
            // Limpiar el VBox antes de agregar los nuevos elementos
            vbox.getChildren().clear();


            // Agregar un elemento por cada host en la lista
            for (ClientInfo host : hostList) {
                if (!host.getNick().equals("enviando")) {
                    HostControlPanel hostControlPanel = new HostControlPanel(host, this.cliente);
                    hostControlPanel.setStyle("-fx-background-color: #2e2e2e;");  // Fondo oscuro para cada panel de host
                    vbox.getChildren().add(hostControlPanel);  // Agregar el panel del host al VBox
                }
            }

            // Desplazarse automáticamente hacia abajo cuando se actualicen los elementos
            scroll.setVvalue(1.0);  // Mover la barra de desplazamiento hacia el final
        });
    }
}
