package org.filetalk.filetalk.test;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DropDownExample extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Crear un botón
        Button toggleButton = new Button("Mostrar/Ocultar Información");

        // Crear un label con la información que se va a mostrar/ocultar
        Label infoLabel = new Label("Aquí va la información desplegada...");
        infoLabel.setVisible(false);  // Inicialmente está oculto

        // Al hacer clic en el botón, cambiar el estado de visibilidad del label
        toggleButton.setOnAction(event -> {
            // Alternar la visibilidad del label
            infoLabel.setVisible(!infoLabel.isVisible());
        });

        // Crear un layout VBox para organizar el botón y el label verticalmente
        VBox root = new VBox(10);  // Espacio de 10 pixeles entre los elementos
        root.getChildren().addAll(toggleButton, infoLabel);  // Agregar el botón y el label al VBox

        // Establecer la escena
        Scene scene = new Scene(root, 300, 200);

        // Configurar y mostrar la ventana
        primaryStage.setTitle("Ejemplo de DropDown");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
