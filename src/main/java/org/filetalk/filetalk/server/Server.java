package org.filetalk.filetalk.server;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import org.filetalk.filetalk.Client.ClientInfo;
import org.filetalk.filetalk.model.Observers.ServerObserver;
import org.filetalk.filetalk.shared.ClientListMessage;
import org.filetalk.filetalk.shared.CommunicationType;
import org.filetalk.filetalk.shared.Mensaje;
import org.filetalk.filetalk.utils.Color;


public class Server {
    // Configuración del puerto y otras variables del servidor
    static final ConcurrentHashMap<Socket, ClientInfo> clients = new ConcurrentHashMap<>();
    private final List<ClientHandler> clientPool = new ArrayList<>();
    private long serverStartTime;
    private final List<String> messageHistory = new ArrayList<>();
    public AtomicLong totalMessagesReceived = new AtomicLong(0);
    public long totalBytesSent = 0;
    private final String startTime = new Date().toString();
    // Códigos de escape ANSI para colores en la consola
    private final String ANSI_RESET = "\u001B[0m"; // Reset
    private final String ANSI_RED = "\u001B[31m";   // Rojo
    private final String ANSI_CYAN = "\u001B[36m";  // Cian
    private ServerObserver serverObserver;
    private static volatile Server serverInstancia;
    private ConfiguracionServidor config = new ConfiguracionServidor();
    private final int PORT = Integer.parseInt(config.obtener("servidor.puerto"));
    private ServerSocket serverSocket;

    // Constructor vacío del servidor

    private Server() {

    }

    // Método que inicia el servidor y maneja las conexiones de los clientes
    public void startServer() {
        serverStartTime = System.currentTimeMillis();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

        // Tarea programada para hacer broadcast del estado del servidor cada 1 segundo
        scheduler.scheduleAtFixedRate(this::brocastServer, 0, 1, TimeUnit.SECONDS);

        scheduler.scheduleAtFixedRate(this::updateStatus, 0, 1, TimeUnit.SECONDS);
        //new Thread(this::administrativeInterface).start();
        // Iniciar el socket del servidor para aceptar conexiones de clientes
        try  {
            serverSocket = new ServerSocket(PORT);
            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();

                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clientPool.add(clientHandler);
                new Thread(clientHandler).start(); // Iniciar un nuevo hilo para manejar al cliente

            }
        } catch (IOException e) {
            System.out.println("Error con el servidor: " + e.getMessage());
        }
    }


    public  void starServerCLI(){

        new Thread(this::administrativeInterface).start();

        startServer();
    }


    public void stopServer() throws IOException {

        clientPool.forEach(ClientHandler::shutDown);

        serverSocket.close();

    }

    // Método para obtener la instancia única del servidor (Singleton)
    public static synchronized Server getInstance() {
        if (serverInstancia == null) {
            synchronized (Server.class) {
                if (serverInstancia == null) {
                    serverInstancia = new Server();
                }
            }
        }
        return serverInstancia;
    }

    public synchronized void setServerObserver(ServerObserver observer){
        synchronized (Server.class) {
            this.serverObserver=observer;
        }
    }

    // Método sincronizado para agregar un mensaje al historial de mensajes
    public synchronized void addMessageHistory(String message) {
        messageHistory.add(message);
    }

    // Método sincronizado para agregar bytes enviados al total
    public synchronized void addBytes(long bytes) {
        totalBytesSent += bytes;
    }

    // Obtener la lista de clientes conectados
    public List<ClientHandler> getClientPool() {

        return clientPool;
    }

    public List<ClientHandler> getClients(){

        System.out.println(clientPool);

        return  clientPool;

    }

    // Método para hacer broadcast del estado del servidor en la red
    private void brocastServer() {
        String broadcastMessage = "[" + getLocalIp() + "]" + "[" + PORT + "]";

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            byte[] buffer = broadcastMessage.getBytes();
            InetAddress broadcastAddress = getBroadcastAddress();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, broadcastAddress, 9092);
            socket.send(packet);


            Thread.sleep(100); // Esperar un poco para asegurar el envío antes de cerrar el socket


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Obtener la dirección IP local del servidor
    private String getLocalIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
            return "Desconocido";
        }
    }

    // Obtener la dirección de broadcast local
    private InetAddress getBroadcastAddress() {
        try {
            InetAddress localAddress = InetAddress.getLocalHost();
            byte[] ip = localAddress.getAddress();
            ip[3] = (byte) 255; // Configurar el último octeto a 255 para el broadcast
            return InetAddress.getByAddress(ip);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Método para la interfaz administrativa del servidor
    private void administrativeInterface() {

        try {
            displayServerStatusWithAnimation();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

    }

    // Método para mostrar el estado del servidor con animación
    private void displayServerStatusWithAnimation() throws UnknownHostException {
        String[] spinner = {"|", "/", "-", "\\"};
        int index = 0;

        while (true) {
            clearConsole();
            serverInfo();
            try {
                String serverStatusV1 = constructServerStatusV1(spinner[index]);
                System.out.print("\r" + serverStatusV1);
                displayClientsInfo();
                System.out.println();
                messageHistory.forEach(System.out::println);
                index = (index + 1) % spinner.length;
                //TimeUnit.MILLISECONDS.sleep(300); // Esperar 1 segundo antes de la próxima actualización
                TimeUnit.SECONDS.sleep(6); // Esperar 1 segundo antes de la próxima actualización
                System.gc(); // Forzar recolección de basura
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }


    private  void serverInfo() throws UnknownHostException {
        String serverVersion = "1.0.0";
        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");
        String javaVersion = System.getProperty("java.version");
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        //String ip= InetAddress.getLocalHost().getHostAddress();
        String ip= getPublicIP();
        long maxMemory = Runtime.getRuntime().maxMemory() / (1024 * 1024 * 1024);
        double maxMemoryMB = Runtime.getRuntime().maxMemory() / (1024.0 * 1024.0);
        String maxMem;
        if (maxMemoryMB >= 1024) {
            maxMem=   String.format(" %.2f GB ", maxMemoryMB / 1024);
        } else {
            maxMem= String.format(" %.2f MB ", maxMemoryMB);
        }
        String linuxLogo = """
                  \t      _nnnn_
                   \t     dGGGGMMb
                   \t    @p~qp~~qMb
                   \t    M|@||@) M|
                   \t    @,----.JM|
                   \t   JS^\\__/  qKL
                   \t  dZP        qKRb
                   \t dZP          qKKb
                   \tfZP            SMMb
                   \tHZM            MMMM
                  \tFqM            MMMM
                 \t__| ".        |\\dS"qML
                 \t|    `.       | `' \\Zq
                \t_)      \\.___.,|     .'
                \t\\____   )MMMMMP|   .'
                    \t `-'       `--'
                """;

        String genericLogo = """
                        _.-;;-._
                      '-..-'|   ||   |
                      '-..-'|_.-;;-._|
                      '-..-'|   ||   |
                      '-..-'|_.-''-._|
                """;

        //String logo = osName.toLowerCase().contains("linux") ? linuxLogo : genericLogo;
        String logo= """
                                                                           X       \s
                 XXXXXX   XX   XX       XXXXXX  XXXX     XX       X        X     XXX
                 X             XX       X        X      XXXX      X        X   XXX \s
                 XXXXX    XX   XX       X        X     XX  XX     X        XXXXX   \s
                 X        XX   XX       XXXXX    X     X    XX    X        XX      \s
                 X        XX   XX       X        X    XXXXXXXX    X        XXXX    \s
                 X        XX   XX       X        X    X      X    X        X  XX   \s
                 X        XX   XX       X        X   XX      X    X        X   XX  \s
                 X        XX   XX       X        X  XX       XX   X        X    XXX\s
                XX        XX  XXXXXXXX  XXXXXX   X  X         X   XXXXXX   X      XX\s
                
                """;
        System.out.println(Color.CYAN_BOLD + "==========================================================");
        System.out.println("                  🌐 Servidor Iniciado 🌐    \uD83D\uDD12              ");
        System.out.println(Color.CYAN_BOLD + "==========================================================");
        System.out.println(Color.RED_BRIGHT +

                logo +"("+ serverVersion + ")" + Color.RESET);

        System.out.println(Color.RED_UNDERLINED +
                "----------------------------------------------------------" + Color.RESET);


        System.out.println(Color.YELLOW_BOLD+"  Versión del Servidor:       " + Color.YELLOW + serverVersion + Color.RESET);
        System.out.println(Color.YELLOW_BOLD+"  Puerto:                     " + Color.YELLOW + PORT + Color.RESET);
        System.out.println(Color.YELLOW_BOLD+"  Ip:                         " + Color.YELLOW + ip + Color.RESET);
        System.out.println(Color.YELLOW_BOLD+"  Hora de Inicio:             " + Color.YELLOW + startTime + Color.RESET);
        System.out.println(Color.YELLOW_BOLD+"  Sistema Operativo:          " + Color.YELLOW + osName + " " + osVersion + Color.RESET);
        System.out.println(Color.YELLOW_BOLD+"  Versión de Java:            " + Color.YELLOW + javaVersion + Color.RESET);
        System.out.println(Color.YELLOW_BOLD+"  Procesadores Disponibles:   " + Color.YELLOW + availableProcessors + Color.RESET);
        System.out.println(Color.YELLOW_BOLD+"  Memoria Máxima Heap:       " + Color.YELLOW +maxMem+ Color.RESET);
        System.out.println(Color.CYAN_BOLD + "==========================================================");
        System.out.println("\uD83D\uDD12  Esperando conexiones en el puerto " + Color.YELLOW + PORT + Color.RESET + "...");
        System.out.println(Color.CYAN_BOLD + "==========================================================");
    }


    // Método para construir el estado del servidor en formato string
    private String constructServerStatusV1(String spinner) {
        return ANSI_CYAN + "------ Estado del Servidor "+spinner+ ANSI_RESET + " " +
                Color.BLUE + "Clientes Conectados: " + clients.size() + ANSI_RESET + " " +
                Color.BLUE + "Mensajes Totales: " + totalMessagesReceived + " " +
                Color.BLUE + "Uptime: " + formatUptime(serverStartTime) + " " +
                Color.BLUE + "Memoria Usada: " + getMemoryUsage() + "\n" +
                ANSI_CYAN + "------ Estado del Servidor ------" + ANSI_RESET + " " +
                Color.BLUE + "Bytes Enviados: " + formatBytes(totalBytesSent) + ANSI_RESET + " " +
                Color.BLUE + "Hilos Activos: " + Thread.activeCount() + ANSI_RESET + " " +
                Color.BLUE + "Configuración del Sistema: " + Runtime.getRuntime().availableProcessors() + " CPU Cores, " +
                formatBytes(Runtime.getRuntime().totalMemory()) + " RAM Heap, OS: " + System.getProperty("os.name") + ANSI_RESET;
    }

    // Limpiar la consola
    private void clearConsole() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            }
        } catch (IOException | InterruptedException e) {
            System.out.println(ANSI_RED + "Error limpiando la consola." + ANSI_RESET);
        }
    }


    public String getPublicIP(){
        try {
            // URL del servicio que devuelve la IP pública
            String url = "https://api.ipify.org"; // También puedes usar "https://checkip.amazonaws.com" o "https://icanhazip.com"

            // Crear el cliente HTTP
            HttpClient client = HttpClient.newHttpClient();

            // Crear la solicitud GET
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();

            // Enviar la solicitud y obtener la respuesta
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Mostrar la IP pública
            System.out.println("IP pública: " + response.body());
            return response.body();
        } catch (Exception e) {

            e.printStackTrace();
            return  "";
        }
    }

    // Método para formatear los bytes a una unidad legible (KB, MB, GB)
    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " Bytes ";
        } else if (bytes < 1024 * 1024) {
            return String.format(" %.2f KB ", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format(" %.2f MB ", bytes / (1024.0 * 1024.0));
        } else {
            return String.format(" %.2f GB ", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    // Mostrar información de los clientes conectados
    private void displayClientsInfo() {
        StringBuilder display = new StringBuilder();
        display.append(Color.YELLOW_BOLD + "============ Clientes Conectados ===========\n");
        display.append("┌─────────────────────┬──────────────────┬─────────────┐\n");
        display.append("│      IP Cliente     │  Tiempo Conexión │    Nick     │\n");
        display.append("├─────────────────────┼──────────────────┼─────────────┤\n");

        for (Map.Entry<Socket, ClientInfo> entry : clients.entrySet()) {
            Socket socket = entry.getKey();
            ClientInfo clientInfo = entry.getValue();

            display.append(String.format("│ %-19s │ %-16s │ %-13s │%n",
                    socket.getInetAddress().getHostAddress(),  // Dirección IP
                    formatUptime(clientInfo.getConnectionTime()),     // Tiempo de conexión
                    clientInfo.getNick()));                       // Nick del cliente
        }

        display.append("├─────────────────────┼──────────────────┼─────────────┤\n");
        display.append("└─────────────────────┴──────────────────┴─────────────┘\n");

        System.out.print("\n" + display.toString());
    }

    // Formatear el tiempo de actividad en horas:minutos:segundos
    private String formatUptime(long time) {
        long uptime = System.currentTimeMillis() - time;
        long seconds = (uptime / 1000) % 60;
        long minutes = (uptime / (1000 * 60)) % 60;
        long hours = (uptime / (1000 * 60 * 60)) % 24;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    // Obtener el uso de memoria heap actual
    private String getMemoryUsage() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryUsage = memoryBean.getHeapMemoryUsage();
        long usedHeapMemory = heapMemoryUsage.getUsed();
        long maxHeapMemory = heapMemoryUsage.getMax();

        double usedMemoryMB = usedHeapMemory / (1024.0 * 1024.0);
        double maxMemoryMB = maxHeapMemory / (1024.0 * 1024.0);

        if (maxMemoryMB >= 1024) {
            return String.format("%.2f MB / %.2f GB", usedMemoryMB, maxMemoryMB / 1024);
        } else {
            return String.format("%.2f MB / %.2f MB", usedMemoryMB, maxMemoryMB);
        }
    }

    // Método sincronizado para enviar un mensaje a todos los clientes, excepto uno
    public synchronized void broadcastMessage(String message, ClientHandler excludeClient) {
        for (ClientHandler client : clientPool) {
            if (client != excludeClient) {
                client.sendComunicacion(new Mensaje( message, CommunicationType.MESSAGE));
            }
        }
    }

    // Actualizar la lista de clientes conectados
    public void updateClient(String nick) {
        List<ClientInfo> clientes = new ArrayList<>();
        for (Map.Entry<Socket, ClientInfo> entry : clients.entrySet()) {
            clientes.add(entry.getValue());
        }

        // Crear el mensaje con la lista de clientes actualizada
        ClientListMessage clientListMessage = new ClientListMessage(
                CommunicationType.UPDATE,  // Tipo de comunicación (en este caso, de tipo "UPDATE")
                clientes
        );

        // Enviar el mensaje a todos los clientes conectados
        for (ClientHandler client : clientPool) {
            client.sendComunicacion(clientListMessage);  // Enviar el ClientListMessage a cada cliente
        }
    }

    public void updateClient() {
        List<ClientInfo> clientes = new ArrayList<>();
        for (Map.Entry<Socket, ClientInfo> entry : clients.entrySet()) {
            clientes.add(entry.getValue());
        }

        // Crear el mensaje con la lista de clientes actualizada
        ClientListMessage clientListMessage = new ClientListMessage(
                CommunicationType.UPDATE,  // Tipo de comunicación (en este caso, de tipo "UPDATE")
                clientes
        );

        // Enviar el mensaje a todos los clientes conectados
        for (ClientHandler client : clientPool) {
            client.sendComunicacion(clientListMessage);  // Enviar el ClientListMessage a cada cliente
        }

    }


    public void addClientUpdate(ClientInfo clientInfo){
        List<ClientInfo>clientInfos=new ArrayList<>(clients.values());
        serverObserver.updateClient(clientInfos, Thread.activeCount());

    }

    private void updateUptime(){
        this.serverObserver.updateUptime(formatUptime(serverStartTime));
    }

    public void updateStatus(){

        Runtime runtime = Runtime.getRuntime();

    // Ejecuta el recolector de basura
            runtime.gc();

    // Total de memoria en JVM (en bytes)
            long totalMemory = runtime.totalMemory();

    // Memoria libre en la JVM (en bytes)
            long freeMemory = runtime.freeMemory();

    // Memoria usada en la JVM (en bytes)
            long usedMemory = totalMemory - freeMemory;

    // Convertir de bytes a gigabytes (1 GB = 1024 * 1024 * 1024 bytes)
            double usedMemoryInGB = (double) usedMemory / (1024 * 1024 * 1024);

            String memoria=String.format("%.2f", usedMemoryInGB);

        this.serverObserver.updateMemory(memoria);
        this.serverObserver.updateUptime(formatUptime(serverStartTime));
        //serverObserver.updateClient(clients.forEach();,Thread.activeCount());
        //clients.forEach(()-> serverObserver.updateClient(t));
        List<ClientInfo>clientInfos=new ArrayList<>(clients.values());
        serverObserver.updateClient(clientInfos, Thread.activeCount());
        //clients.forEach((clave,valor)->serverObserver.updateClient(valor, Thread.activeCount()));
    }

    public void updateBytes() {

        this.serverObserver.updateBytes(formatBytes(totalBytesSent));
    }
    public int getPORT() {
        return PORT;
    }
}

