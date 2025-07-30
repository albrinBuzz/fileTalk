package org.filetalk.filetalk.view.hosts;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.filetalk.filetalk.Client.Client;
import org.filetalk.filetalk.model.Observers.Observer;
import org.filetalk.filetalk.shared.ServerStatusConnection;

import java.io.IOException;
import java.util.List;

public class ChatPanel extends VBox implements Observer {

    private VBox chatBox;
    private TextArea chatArea;
    private TextField inputChat;
    private Button sendChatBtn;
    private Client client;
    public ChatPanel(Client client) {
        this.client=client;
        // Crear el VBox que contendrá todo el chat
        chatBox = new VBox(5);
        chatBox.setPadding(new Insets(10));
        chatBox.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 1;");

        // Título del chat
        Label chatLabel = new Label("💬 Chat y coordinación de transferencias");
        chatLabel.setFont(Font.font(14));
        chatLabel.setStyle("-fx-font-weight: bold;");

        // Usuarios en el chat
        Label usuariosChatLabel = new Label("[Usuarios en chat: Ana Torres, Carlos Méndez, Andrea Ruiz]");
        usuariosChatLabel.setStyle("-fx-font-style: italic;");

        // Área de chat (solo lectura)
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setPrefHeight(120);
        /*chatArea.setText(
                "Ana Torres (🟢 Online):\n" +
                        "— Juan, ¿puedes enviarme el plan actualizado?\n" +
                        "— Estoy enviando el archivo \"plan_proyecto.pdf\" ahora.\n\n" +
                        "Carlos Méndez (🟠 Ocupado):\n" +
                        "— Acabo de recibir el informe, revisando y respondo.\n\n" +
                        "Andrea Ruiz (🟢 Online):\n" +
                        "— Preparando carpeta \"documentos_marketing\" para enviar.\n"
        );*/

        // Caja de entrada de texto y botón de envío
        HBox inputChatBox = new HBox(5);
        inputChat = new TextField();
        inputChat.setPromptText("Escribe un mensaje...");
        sendChatBtn = new Button("Enviar");
        sendChatBtn.setOnAction(e -> sendMessage());
        inputChatBox.getChildren().addAll(inputChat, sendChatBtn);

        // Añadir todos los elementos al VBox del chat
        chatBox.getChildren().addAll(chatLabel, chatArea, inputChatBox);

        // Añadir el VBox del chat al contenedor principal
        this.getChildren().add(chatBox);
    }

    // Método para enviar un mensaje al chat
    public void sendMessage() {
        String message = inputChat.getText().trim();
        if (!message.isEmpty()) {
            //String formattedMessage = formatMessage("Tú", message);
            chatArea.appendText(message);
            inputChat.clear();
            try {
                client.enviarMensaje(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Desplazar la vista hacia abajo
            chatArea.setScrollTop(Double.MAX_VALUE);  // Desplazamiento automático al final
        }
    }




    // Método para actualizar los usuarios del chat
    public void updateUsuariosChat(String usuarios) {
        Label usuariosChatLabel = (Label) chatBox.getChildren().get(1);  // Acceder al label de usuarios
        usuariosChatLabel.setText("[Usuarios en chat: " + usuarios + "]");
    }

    // Getter para el botón de enviar
    public Button getSendChatBtn() {
        return sendChatBtn;
    }

    @Override
    public void updateServerConnection(ServerStatusConnection statusConnection) {

    }

    @Override
    public void updateClientsList(List<String> clients) {

    }

    @Override
    public void updateMessaje(String message) {
        Platform.runLater(() -> {
            appendMessage(message, false);  // Llama a la función para agregar el mensaje
            chatArea.setScrollTop(Double.MAX_VALUE);
        });
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
                chatArea.appendText(message+"\n");
                messageArea.setStyle("-fx-background-color: lightblue; -fx-alignment: center-right;");
                messageContainer.getChildren().add(messageArea);
                messageContainer.setStyle("-fx-alignment: center-right;"); // Alinea a la derecha
            } else {
                chatArea.appendText(message+"\n");
                messageArea.setStyle("-fx-background-color: lightgray; -fx-alignment: center-left;");
                messageContainer.getChildren().add(messageArea);
                messageContainer.setStyle("-fx-alignment: center-left;"); // Alinea a la izquierda
            }

            /*vboxMsj.getChildren().add(messageContainer);
            vboxMsj.heightProperty().addListener((obs, oldVal, newVal) -> {
                scrollMsj.setVvalue(1.0); // Desplazar hacia abajo al agregar un nuevo mensaje
            });*/
        });


    }
}
