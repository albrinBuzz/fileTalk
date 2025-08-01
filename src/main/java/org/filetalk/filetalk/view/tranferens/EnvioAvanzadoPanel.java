package org.filetalk.filetalk.view.tranferens;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EnvioAvanzadoPanel extends VBox {

    private final TextField destinatariosField = new TextField();
    private final TextField archivosField = new TextField();
    private final TextField mensajeField = new TextField();
    private final TextField tamFragmentoField = new TextField("50");
    private final ComboBox<String> compresionCombo = new ComboBox<>();
    private final CheckBox fragmentarCb = new CheckBox("Fragmentar archivos grandes (>100MB)");
    private final CheckBox carpetaDestinoCb = new CheckBox("Permitir seleccionar carpeta destino en receptor");
    private final CheckBox checksumCb = new CheckBox("Confirmar integridad con checksum (SHA-256)");
    private final DatePicker expiracionDate = new DatePicker();
    private final TextField horaExpField = new TextField();
    private final CheckBox confirmacionCb = new CheckBox("Solicitar confirmación de recepción");
    private final CheckBox sobrescribirCb = new CheckBox("Sobrescribir archivos existentes sin preguntar");
    private final CheckBox modoSilenciosoCb = new CheckBox("Enviar en modo silencioso (sin notificaciones)");
    private final ComboBox<String> qosCombo = new ComboBox<>();
    private final TextField etiquetasField = new TextField();
    private final ComboBox<String> visibilidadCombo = new ComboBox<>();
    private final PasswordField contraseñaField = new PasswordField();
    private final CheckBox notificarEmailCb = new CheckBox("Enviar notificación por email al receptor");
    private final TextField emailField = new TextField();
    private final CheckBox reintentoCb = new CheckBox("Reintento automático si falla el envío");
    private final TextArea comentariosArea = new TextArea();
    private final RadioButton rbArchivo = new RadioButton("Archivo");
    private final RadioButton rbDirectorio = new RadioButton("Directorio");
    private final CheckBox incluirSubdirsCb = new CheckBox("Incluir subdirectorios");
    private final TextField limiteVelocidadField = new TextField();
    private final ComboBox<String> confidencialidadCombo = new ComboBox<>();
    private final ComboBox<String> encriptacionCombo = new ComboBox<>();
    private final DatePicker envioProgramadoDate = new DatePicker();
    private final TextField envioProgramadoHora = new TextField();

    private final ComboBox<String> tipoEnvioCombo = new ComboBox<>();
    private final List<File> archivosSeleccionados = new ArrayList<>();

    // Por estas nuevas líneas:
    private final ComboBox<String> destinatarioComboBox = new ComboBox<>();
    private final Button recargarUsuariosBtn = new Button("⟳ Actualizar usuarios");

    // Lista simulada de usuarios disponibles
    private List<String> usuariosDisponibles = List.of("Ana Torres", "Carlos Méndez", "Andrea Ruiz");


    public EnvioAvanzadoPanel() {
        this.setSpacing(10);
        this.setPadding(new Insets(10));
        this.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 1;");

        Label titulo = new Label("📤 Envío de archivo avanzado");
        titulo.setFont(Font.font(15));
        titulo.setStyle("-fx-font-weight: bold;");

        GridPane grid = construirFormulario();
        HBox botones = construirBotones();

        this.getChildren().addAll(titulo, grid, botones);
    }

    private GridPane construirFormulario() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.getColumnConstraints().addAll(new ColumnConstraints(), new ColumnConstraints());

        // --- Columna izquierda ---

        destinatariosField.setPromptText("Nombres o IPs, separados por coma");

        archivosField.setPromptText("Ruta seleccionada");
        archivosField.setEditable(false);

        tipoEnvioCombo.getItems().addAll("Archivo", "Directorio");
        tipoEnvioCombo.setValue("Archivo");

        Button seleccionarBtn = new Button("Seleccionar");
        seleccionarBtn.setOnAction(e -> seleccionarArchivoODirectorio());

        HBox archivosBox = new HBox(5, archivosField, tipoEnvioCombo, seleccionarBtn);
        archivosBox.setAlignment(Pos.CENTER_LEFT);

        mensajeField.setPromptText("Mensaje para destinatario");

        tamFragmentoField.setPrefWidth(60);
        HBox fragmentarBox = new HBox(5, fragmentarCb, new Label("Tamaño fragmento (MB):"), tamFragmentoField);
        fragmentarBox.setAlignment(Pos.CENTER_LEFT);

        compresionCombo.getItems().addAll("Sin compresión", "ZIP", "TAR");
        compresionCombo.setValue("Sin compresión");

        // En el constructor, después de `titulo.setStyle(...)` agrega:
        destinatarioComboBox.getItems().addAll(usuariosDisponibles);
        destinatarioComboBox.setEditable(true); // Permite escribir manualmente si no está en la lista
        destinatarioComboBox.setPromptText("Selecciona o escribe un usuario...");

        recargarUsuariosBtn.setOnAction(e -> recargarUsuarios());

        // Añadir a columna izquierda
        HBox destinatarioBox = new HBox(5, destinatarioComboBox, recargarUsuariosBtn);
        destinatarioBox.setAlignment(Pos.CENTER_LEFT);
        //grid.add(new Label("Destinatario:"), 0, 0);
        grid.add(destinatariosField, 0, 0);
        grid.add(destinatarioBox, 0, 1);
        //grid.add(new Label("Destinatarios:"), 0, 0);
        grid.add(new Label("Archivos / Directorio:"), 0, 2);
        grid.add(archivosBox, 0, 3);
        grid.add(new Label("Mensaje:"), 0, 4);
        grid.add(mensajeField, 0, 5);
        grid.add(fragmentarBox, 0, 6);
        grid.add(new Label("Compresión:"), 0, 7);
        grid.add(compresionCombo, 0, 8);
        grid.add(carpetaDestinoCb, 0, 9);
        grid.add(checksumCb, 0, 10);

        // --- Columna derecha ---

        horaExpField.setPromptText("HH:mm");
        horaExpField.setPrefWidth(60);
        HBox expiracionBox = new HBox(5, expiracionDate, new Label("Hora (HH:mm):"), horaExpField);
        expiracionBox.setAlignment(Pos.CENTER_LEFT);

        qosCombo.getItems().addAll("Baja", "Normal", "Alta");
        qosCombo.setValue("Normal");

        visibilidadCombo.getItems().addAll("Privado", "Público", "Protegido con contraseña");
        visibilidadCombo.setValue("Privado");
        contraseñaField.setDisable(true);

        visibilidadCombo.setOnAction(e -> {
            boolean requierePw = "Protegido con contraseña".equals(visibilidadCombo.getValue());
            contraseñaField.setDisable(!requierePw);
            if (!requierePw) contraseñaField.clear();
        });

        emailField.setPromptText("Email del receptor");
        emailField.setDisable(true);

        notificarEmailCb.setOnAction(e -> {
            emailField.setDisable(!notificarEmailCb.isSelected());
            if (!notificarEmailCb.isSelected()) emailField.clear();
        });

        comentariosArea.setPrefRowCount(3);
        comentariosArea.setPromptText("Comentarios adicionales...");

        ToggleGroup tipoEnvioGroup = new ToggleGroup();
        rbArchivo.setToggleGroup(tipoEnvioGroup);
        rbDirectorio.setToggleGroup(tipoEnvioGroup);
        rbArchivo.setSelected(true);

        tipoEnvioGroup.selectedToggleProperty().addListener((obs, old, nuevo) -> {
            incluirSubdirsCb.setDisable(nuevo != rbDirectorio);
            if (nuevo != rbDirectorio) incluirSubdirsCb.setSelected(false);
        });

        limiteVelocidadField.setPromptText("Sin límite");
        limiteVelocidadField.setPrefWidth(80);

        confidencialidadCombo.getItems().addAll("Público", "Interno", "Confidencial", "Secreto");
        confidencialidadCombo.setValue("Interno");

        encriptacionCombo.getItems().addAll("Ninguna", "AES-128", "AES-256", "RSA");
        encriptacionCombo.setValue("Ninguna");

        envioProgramadoHora.setPromptText("HH:mm");
        envioProgramadoHora.setPrefWidth(60);
        HBox envioProgramadoBox = new HBox(5, envioProgramadoDate, envioProgramadoHora);
        envioProgramadoBox.setAlignment(Pos.CENTER_LEFT);

        HBox tipoEnvioBox = new HBox(10, rbArchivo, rbDirectorio);
        tipoEnvioBox.setAlignment(Pos.CENTER_LEFT);

        int baseRow = 18;

        grid.add(new Label("Fecha de expiración:"), 1, 0);
        grid.add(expiracionBox, 1, 1);
        grid.add(confirmacionCb, 1, 2);
        grid.add(sobrescribirCb, 1, 3);
        grid.add(modoSilenciosoCb, 1, 4);
        grid.add(new Label("QoS:"), 1, 5);
        grid.add(qosCombo, 1, 6);
        grid.add(new Label("Etiquetas:"), 1, 7);
        grid.add(etiquetasField, 1, 8);
        grid.add(new Label("Visibilidad:"), 1, 9);
        grid.add(visibilidadCombo, 1, 10);
        grid.add(new Label("Contraseña:"), 1, 11);
        grid.add(contraseñaField, 1, 12);
        grid.add(notificarEmailCb, 1, 13);
        grid.add(emailField, 1, 14);
        grid.add(reintentoCb, 1, 15);
        grid.add(new Label("Comentarios:"), 1, 16);
        grid.add(comentariosArea, 1, 17);
        grid.add(new Label("Tipo envío:"), 1, baseRow);
        grid.add(tipoEnvioBox, 1, baseRow + 1);
        grid.add(incluirSubdirsCb, 1, baseRow + 2);
        grid.add(new Label("Límite de velocidad (KB/s):"), 1, baseRow + 3);
        grid.add(limiteVelocidadField, 1, baseRow + 4);
        grid.add(new Label("Confidencialidad:"), 1, baseRow + 5);
        grid.add(confidencialidadCombo, 1, baseRow + 6);
        grid.add(new Label("Encriptación:"), 1, baseRow + 7);
        grid.add(encriptacionCombo, 1, baseRow + 8);
        grid.add(new Label("Envío programado:"), 1, baseRow + 9);
        grid.add(envioProgramadoBox, 1, baseRow + 10);

        return grid;
    }

    private void recargarUsuarios() {
        // Aquí podrías consultar a un servidor para obtener la lista actualizada de usuarios
        usuariosDisponibles = List.of("Ana Torres", "Carlos Méndez", "Andrea Ruiz", "Nuevo Usuario");

        destinatarioComboBox.getItems().setAll(usuariosDisponibles);
        mostrarInfo("Usuarios actualizados.");
    }


    private HBox construirBotones() {
        Button enviarBtn = new Button("Enviar");
        Button limpiarBtn = new Button("Limpiar");
        enviarBtn.setOnAction(e -> enviar());
        limpiarBtn.setOnAction(e -> limpiarFormulario());
        HBox box = new HBox(10, enviarBtn, limpiarBtn);
        box.setAlignment(Pos.CENTER_RIGHT);
        return box;
    }

    private void seleccionarArchivoODirectorio() {
        Stage stage = (Stage) this.getScene().getWindow();
        archivosSeleccionados.clear();
        archivosField.clear();

        if ("Archivo".equals(tipoEnvioCombo.getValue())) {
            FileChooser fileChooser = new FileChooser();
            List<File> files = fileChooser.showOpenMultipleDialog(stage);
            if (files != null) {
                archivosSeleccionados.addAll(files);
                archivosField.setText(
                        archivosSeleccionados.stream()
                                .map(File::getAbsolutePath)
                                .collect(Collectors.joining(", "))
                );
            }
        } else {
            DirectoryChooser dirChooser = new DirectoryChooser();
            File dir = dirChooser.showDialog(stage);
            if (dir != null) {
                archivosSeleccionados.add(dir);
                archivosField.setText(dir.getAbsolutePath());
            }
        }
    }

    private void enviar() {
        // Validaciones mínimas
        if (destinatariosField.getText().isEmpty()) {
            mostrarError("Debes indicar al menos un destinatario.");
            return;
        }
        if (archivosSeleccionados.isEmpty()) {
            mostrarError("Debes seleccionar al menos un archivo o directorio.");
            return;
        }

        // Aquí podrías enviar los datos a tu backend o servicio de red
        mostrarInfo("Formulario enviado correctamente.");
    }

    private void limpiarFormulario() {
        destinatariosField.clear();
        archivosField.clear();
        mensajeField.clear();
        tamFragmentoField.setText("50");
        compresionCombo.setValue("Sin compresión");
        fragmentarCb.setSelected(false);
        carpetaDestinoCb.setSelected(false);
        checksumCb.setSelected(false);
        expiracionDate.setValue(null);
        horaExpField.clear();
        confirmacionCb.setSelected(false);
        sobrescribirCb.setSelected(false);
        modoSilenciosoCb.setSelected(false);
        qosCombo.setValue("Normal");
        etiquetasField.clear();
        visibilidadCombo.setValue("Privado");
        contraseñaField.clear();
        contraseñaField.setDisable(true);
        notificarEmailCb.setSelected(false);
        emailField.clear();
        emailField.setDisable(true);
        reintentoCb.setSelected(false);
        comentariosArea.clear();
        rbArchivo.setSelected(true);
        incluirSubdirsCb.setSelected(false);
        incluirSubdirsCb.setDisable(true);
        limiteVelocidadField.clear();
        confidencialidadCombo.setValue("Interno");
        encriptacionCombo.setValue("Ninguna");
        envioProgramadoDate.setValue(null);
        envioProgramadoHora.clear();
        archivosSeleccionados.clear();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
