package org.filetalk.filetalk.Tests.Gui;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.control.Label;

public class UserItem {

    public static HBox createUserItem(String nombre, String estado, String ultimaConexion, boolean mostrarArchivos) {
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

        Button enviarBtn = new Button("📤 Enviar");
        Button archivosBtn = new Button("📁 Archivos");

        userItem.getChildren().addAll(icon, nameLabel, lastConn, spacer, enviarBtn, archivosBtn);
        return userItem;
    }
}
