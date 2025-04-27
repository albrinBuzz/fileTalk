package org.filetalk.filetalk;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import org.filetalk.filetalk.shared.Logger;

import java.io.IOException;

public class MainView extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Cargar el archivo FXML para la interfaz de usuario
        FXMLLoader fxmlLoader = new FXMLLoader(MainView.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        // Obtener el tamaño de la pantalla disponible
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        Logger.logInfo(bounds.getWidth()+"");
        Logger.logInfo(bounds.getHeight()+"");
        // Ajustar el tamaño de la ventana al tamaño de la pantalla
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());

        // Configurar otras propiedades de la ventana
        stage.setTitle("FileTalk!");  // Título de la ventana
        stage.setScene(scene);  // Establecer la escena en el escenario
        stage.setMaximized(true);  // Maximizar la ventana cuando se inicie
        stage.setResizable(true);
        stage.setScene(scene);  // Establecer la escena en el escenario
        stage.show();  // Mostrar la ventana
    }

    public static void main(String[] args) {
        launch();
    }
}
