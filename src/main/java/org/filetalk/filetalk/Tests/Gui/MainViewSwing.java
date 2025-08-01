package org.filetalk.filetalk.Tests.Gui;

import javax.swing.*;
import java.awt.*;

public class MainViewSwing {

    private JFrame mainFrame;
    private JComboBox<String> temaCombo;
    private JLabel serverStatusLabel, connectionStatusLabel, connectionText;
    private JButton startServerBtn, conectServerBtn, autoConnectBtn;
    private JTextField ipField, puertoField, searchUser, mensajeField;
    private JPanel topPanel;  // Nuevo panel para la parte superior
    private JPanel quickSendPanel, transferPanel, notificationPanel;

    public MainViewSwing() {
        mainFrame = new JFrame("App de Transferencias - Vista Principal");
        mainFrame.setSize(1000, 800);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new BorderLayout());

        // Inicialización de los componentes
        initializeComponents();

        // Mostrar la ventana
        mainFrame.setVisible(true);
    }

    private void initializeComponents() {
        // HEADER
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS)); // BoxLayout en el eje X
        header.setBackground(new Color(44, 62, 80)); // Color #2c3e50

        JLabel userLabel = new JLabel("👤 Juan Ortega");
        userLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        userLabel.setForeground(Color.WHITE);

        JLabel statusLabel = new JLabel("[🟢 Disponible ▼]");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        statusLabel.setForeground(new Color(144, 238, 144)); // LightGreen

        header.add(userLabel);
        header.add(statusLabel);

        // Espaciador para empujar los botones hacia la derecha
        header.add(Box.createHorizontalGlue());

        // Botones a la derecha
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonsPanel.setBackground(new Color(44, 62, 80)); // Color #2c3e50
        JButton configBtn = new JButton("⚙️ Configuración");
        JButton securityBtn = new JButton("🔒 Seguridad");
        JButton logoutBtn = new JButton("🚪 Cerrar sesión");

        buttonsPanel.add(configBtn);
        buttonsPanel.add(securityBtn);
        buttonsPanel.add(logoutBtn);

        header.add(buttonsPanel);

        // CONEXIÓN AL SERVIDOR
        JPanel connectionStatus = new JPanel();
        connectionStatus.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 8));
        connectionStatus.setBackground(new Color(44, 62, 80)); // Color #2c3e50

        connectionText = new JLabel("🔌 Estado de Conexión:");
        connectionText.setForeground(Color.WHITE);

        connectionStatusLabel = new JLabel("[🟢 Desconectado]");
        connectionStatusLabel.setForeground(Color.RED);

        ipField = new JTextField("192.168.100.111", 15);
        puertoField = new JTextField("8080", 6);

        JLabel latencyLabel = new JLabel("Latencia: 35 ms");
        latencyLabel.setForeground(Color.WHITE);

        conectServerBtn = new JButton("🔄 Conectar");

        startServerBtn = new JButton("🔛 Iniciar Servidor");
        serverStatusLabel = new JLabel("[Servidor: Detenido]");
        serverStatusLabel.setForeground(Color.RED);

        autoConnectBtn = new JButton("🔌 Conectar Automáticamente");

        connectionStatus.add(connectionText);
        connectionStatus.add(connectionStatusLabel);
        connectionStatus.add(new JLabel("IP:"));
        connectionStatus.add(ipField);
        connectionStatus.add(new JLabel("Puerto:"));
        connectionStatus.add(puertoField);
        connectionStatus.add(latencyLabel);
        connectionStatus.add(conectServerBtn);

        // Añadir espaciador para botones de conexión
        connectionStatus.add(Box.createHorizontalStrut(560));

        // Botones a la derecha
        JPanel connectionButtonsPanel = new JPanel();
        connectionButtonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        connectionButtonsPanel.setBackground(new Color(44, 62, 80)); // Color #2c3e50
        connectionButtonsPanel.add(autoConnectBtn);
        connectionButtonsPanel.add(serverStatusLabel);
        connectionButtonsPanel.add(startServerBtn);

        connectionStatus.add(connectionButtonsPanel);

        // TOP MENU
        JPanel topMenu = new JPanel();
        topMenu.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topMenu.setBackground(Color.LIGHT_GRAY);

        searchUser = new JTextField(20);
        searchUser.setToolTipText("Buscar usuario...");
        JButton btnMisArchivos = new JButton("📁 Mis archivos");
        JButton btnEnviarArchivo = new JButton("📤 Enviar archivo");
        JButton btnTransferencias = new JButton("📤 Transferencias");

        // ComboBox para seleccionar el tema
        temaCombo = new JComboBox<>(new String[]{"Claro", "Oscuro"});
        temaCombo.setSelectedItem("Claro");

        topMenu.add(searchUser);
        topMenu.add(btnMisArchivos);
        topMenu.add(btnEnviarArchivo);
        topMenu.add(btnTransferencias);
        topMenu.add(new JLabel("Tema:"));
        topMenu.add(temaCombo);

        // PANEL SUPERIOR (agregar HEADER, CONEXIÓN y TOP MENU)
        topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(header);
        topPanel.add(connectionStatus);
        topPanel.add(topMenu);

        mainFrame.add(topPanel, BorderLayout.NORTH);

        // --------- Envío rápido de archivo --------------
        quickSendPanel = new JPanel();
        quickSendPanel.setLayout(new BoxLayout(quickSendPanel, BoxLayout.Y_AXIS));
        quickSendPanel.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));

        JLabel envioLabel = new JLabel("🚀 Envío rápido de archivo");
        envioLabel.setFont(new Font("Arial", Font.BOLD, 14));

        mensajeField = new JTextField(30);
        mensajeField.setToolTipText("Mensaje");

        JButton enviarBtn = new JButton("Enviar archivo");
        JButton cancelarBtn = new JButton("Cancelar");

        quickSendPanel.add(envioLabel);
        quickSendPanel.add(mensajeField);
        quickSendPanel.add(enviarBtn);
        quickSendPanel.add(cancelarBtn);

        // Agregar panel de envío rápido a la ventana principal
        mainFrame.add(quickSendPanel, BorderLayout.EAST);

        // Transferencias y notificaciones
        transferPanel = new JPanel();
        transferPanel.setLayout(new BoxLayout(transferPanel, BoxLayout.Y_AXIS));

        JLabel transferLabel = new JLabel("🔄 Últimas Transferencias ▸");
        transferLabel.setFont(new Font("Arial", Font.BOLD, 14));

        // Lista de transferencias
        JList<String> transferList = new JList<>(new String[]{
                "✅ contrato_final.pdf enviado a Ana Torres (17/07/2025 10:22)",
                "⏳ informe_ventas.xlsx enviado a Carlos Méndez (esperando aceptación)",
                "❌ documento_confidencial.doc rechazado por Pablo Gil",
                "⏳ imagen_producto.png transferencia activa 40% [██████░░░░░░░] 2.0 MB / 4.8 MB 350 KB/s 00:08:45"
        });
        JScrollPane transferScroll = new JScrollPane(transferList);

        transferPanel.add(transferLabel);
        transferPanel.add(transferScroll);

        notificationPanel = new JPanel();
        notificationPanel.setLayout(new BoxLayout(notificationPanel, BoxLayout.Y_AXIS));

        JLabel notifLabel = new JLabel("🔔 Notificaciones ▸");
        notifLabel.setFont(new Font("Arial", Font.BOLD, 14));

        // Lista de notificaciones
        JList<String> notifList = new JList<>(new String[]{
                "📩 Has recibido \"reporte_julio.pdf\" de Andrea",
                "⚠️ Tu espacio está al 95% de uso",
                "🔔 Resumen semanal: 45 archivos enviados, 32 recibidos, 3 transferencias fallidas"
        });
        JScrollPane notifScroll = new JScrollPane(notifList);

        notificationPanel.add(notifLabel);
        notificationPanel.add(notifScroll);

        // Agregar paneles de transferencias y notificaciones
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.add(transferPanel);
        bottomPanel.add(notificationPanel);

        // Agregar el panel inferior
        mainFrame.add(bottomPanel, BorderLayout.SOUTH);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainViewSwing());
    }
}
