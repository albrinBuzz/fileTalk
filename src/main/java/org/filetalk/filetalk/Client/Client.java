package org.filetalk.filetalk.Client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import org.filetalk.filetalk.model.Observers.HostsObserver;
import org.filetalk.filetalk.model.Observers.TransferencesObserver;
import org.filetalk.filetalk.model.Observers.Observer;
import org.filetalk.filetalk.shared.*;
import org.filetalk.filetalk.utils.FileTransferHandler;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Client {
    private List<Observer> observers = new ArrayList<>();
    private List<HostsObserver>hostsObservers=new ArrayList<>();
    private  TransferencesObserver transferencesObserver;
    private String SERVER_ADDRESS;
    private int SERVER_PORT;
    private Socket socket;
    private ObjectInputStream entrada;
    private ObjectOutputStream salida;
    private String msj;
    private ExecutorService executorService;
    private DatagramSocket socketUdp;
    private Servidor servidor;
    private Observer observer;

    public Client(){
        this.executorService = Executors.newFixedThreadPool(10); // Usar un pool de hilos para manejar tareas concurrentes
    }



    public void setConexion(String SERVER_ADDRESS, int SERVER_PORT) throws IOException, InterruptedException {
        this.SERVER_ADDRESS = SERVER_ADDRESS;
        this.SERVER_PORT = SERVER_PORT;
        socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        InetAddress inetAddress = InetAddress.getLocalHost();

        // Obtener el nombre de la máquina
        String hostName = inetAddress.getHostName();

        System.out.println("El nombre de la máquina es: " + hostName);
        salida=new ObjectOutputStream(socket.getOutputStream());
        entrada=new ObjectInputStream(socket.getInputStream());
        salida.writeObject(new Mensaje(hostName, CommunicationType.MESSAGE));
        TimeUnit.MILLISECONDS.sleep(500); // Esperar 1 segundo antes de la próxima actualización
        salida.flush();

        // Iniciar hilos para leer mensajes y recibir archivos
        executorService.submit(new ReadMessages(entrada));
        servidor=new Servidor(9091);
        executorService.submit(servidor); // Suponiendo que este es el puerto para recibir archivos
        cerrarBusqueda();
    }

    public void conexionAutomatica() throws IOException, InterruptedException {
         socketUdp = new DatagramSocket(9092);

        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
       
        socketUdp.receive(packet); // Recibe el mensaje de broadcast
        String received = new String(packet.getData(), 0, packet.getLength());
        String[] argsServer = received.split("\\[|]");
        SERVER_ADDRESS = argsServer[1];
        SERVER_PORT = Integer.parseInt(argsServer[3]);
        setConexion(SERVER_ADDRESS, SERVER_PORT);

    }

    public void cerrarBusqueda(){
        socketUdp.close();
    }

    public void desconect() {
        try {
            // Verificar si la salida y el socket no están ya cerrados
            if (socket != null && !socket.isClosed()) {
                salida.writeObject(new Mensaje(CommunicationType.DISCONNECT));
            }

            // Solo cerrar el socket y la entrada si no están ya cerrados
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

            if (entrada != null) {
                entrada.close();
            }

            // Llamada al método para desconectar del servidor
            this.servidor.disconect();

            // Actualización del estado de la conexión en el observador
            this.observer.updateServerConnection(ServerStatusConnection.DISCONNECTED);

        } catch (IOException e) {
            // Manejo de la excepción
            System.err.println("Error al desconectar: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void handleFileTransfer(String message) throws IOException {
        String[] messageSplit = message.split(" ");
        if (messageSplit.length < 3) {
            System.out.println("Usage: /file <RecipientNick> <FilePath>");
            return;
        }

        String recipientNick = messageSplit[1];
        String filePath = message.substring(message.lastIndexOf(" ") + 1).trim();
        File file = new File(filePath);

        if (!file.exists() || !file.isFile()) {
            System.out.println("Invalid file path");
            return;
        }

        //new Thread(()->sendFile(file,message)).start();
        FileTransferManager transferManager =new FileTransferManager(transferencesObserver);

        executorService.submit(()->transferManager.sendFile(file, message, SERVER_ADDRESS));

        //sendFile(file,message);

    }

    public void enviarMensaje(String mensaje) throws IOException {
        System.out.printf(" [ "+mensaje+" ] ");
        salida.writeObject(new Mensaje(mensaje,CommunicationType.MESSAGE));
    }




    public void addObserver(Observer observer) {
       this.observer = observer;
    }
    public void addHostOserver(HostsObserver observer){
        hostsObservers.add(observer);
    }


    // Notificar a todos los observadores
    private void notifyObservers() {

        this.observer.updateMessaje(msj);
    }

    private void notifyHostobserves(List<ClientInfo>hosts) {

        for (HostsObserver observer : hostsObservers) {
                    observer.updateAllHosts(hosts);  // Notifica a los observadores con el nuevo mensaje
        }
    }

    public void setTransferencesObserver(TransferencesObserver transferencesObserver){
        this.transferencesObserver=transferencesObserver;
    }


    // Cuando llega un mensaje nuevo
    private void handleIncomingMessage(String message) {
        this.msj = message;
        notifyObservers();  // Notificar a los observadores que hay un nuevo mensaje
    }



    // Hilo que lee los mensajes del servidor
    private class ReadMessages implements Runnable {
        private final ObjectInputStream entrada;
        private  Communication communication;

        public ReadMessages(ObjectInputStream entrada) {
            this.entrada = entrada;
        }

        @Override
        public void run() {
            try {
                System.out.println("conectado");

                while (socket.isConnected()) {

                    Object object = entrada.readObject();
                    if (object != null) {

                        communication = (Communication) object;
                        if (communication instanceof ClientListMessage) {
                            ClientListMessage listCliets = (ClientListMessage) communication;
                            //clienteConectados = listCliets.getClientNicks();
                            //actualizarClientesUI(); // Actualiza la UI con los clientes conectados
                            notifyHostobserves(listCliets.getClientNicks());
                        } else {
                            // Si el mensaje es otro tipo de mensaje
                            Mensaje mensaje=(Mensaje) communication;
                            msj =mensaje.getContenido();
                            handleIncomingMessage(msj);  // Procesamos el mensaje recibido
                            // actualizarUIConMensaje(msj); // Actualiza la UI con el mensaje recibido
                        }
                    }else {
                        logInfo("Mensaje nulo");
                    }
                }

            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error leyendo del servidor: " + e.getMessage());
                e.printStackTrace();
            } finally {
                System.out.println("cerrando");
                cleanUp();
            }
        }


        public void cleanUp() {
            try {
                if (entrada != null) {
                    entrada.close();
                }
                if (socket != null) {
                    socket.close();
                }
                notifyHostobserves(new ArrayList<>());
                desconect();
            } catch (IOException e) {
                System.out.println("Error al cerrar recursos: " + e.getMessage());
            }
        }
    }

    // Servidor que recibe los archivos en un puerto diferente
     class Servidor implements Runnable {
        private int port;
        private ServerSocket serverSocket;
        public Servidor(int port) {
            this.port = port;
        }

        @Override
        public void run() {
            try  {
                serverSocket = new ServerSocket(port);
                System.out.println("Servidor escuchando en el puerto: " + port);
                while (!serverSocket.isClosed()) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Conexión aceptada de un cliente.");
                    FileTransferManager transferManager =new FileTransferManager(transferencesObserver);
                    new Thread(() -> transferManager.receiveFiles(clientSocket)).start();
                }
            } catch (IOException e) {
                System.err.println("Error en el servidor: " + e.getMessage());
            }
        }

        public void disconect() throws IOException {
            this.serverSocket.close();
        }

    }

    private void logInfo(String message) {
        // Obtención de la clase, el método y la línea mediante el StackTrace
        StackTraceElement element = Thread.currentThread().getStackTrace()[2];
        String className = element.getClassName();
        String methodName = element.getMethodName();
        int lineNumber = element.getLineNumber();

        // Registro de la información con detalles de la clase, el método y la línea
        //LOGGER.info("Clase: {}, Método: {}, Línea: {} - {}", className, methodName, lineNumber, message);
        System.out.println("Clase: "+className+" Metodo: "+methodName+" Linea: "+lineNumber+" Log: "+message);
    }

}
