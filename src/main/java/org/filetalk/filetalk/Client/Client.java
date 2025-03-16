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
    private ObjectOutputStream out;
    //private BufferedReader in;
    //private DataInputStream entrada;
    private ObjectInputStream entrada;
    private DataOutputStream salidaStream;
    private ObjectOutputStream salida;
    private String msj;
    private List<ClientInfo> clienteConectados;
    private TextArea chatArea; // Para mostrar mensajes
    private ListView<String> clientesListView; // Para mostrar clientes conectados
    private ExecutorService executorService;

    public Client(){
        this.executorService = Executors.newFixedThreadPool(10); // Usar un pool de hilos para manejar tareas concurrentes
        this.clienteConectados = new ArrayList<>();
    }


    public Client( ListView<String> clientesListView) {

        this.clientesListView = clientesListView;
        this.executorService = Executors.newFixedThreadPool(10); // Usar un pool de hilos para manejar tareas concurrentes
        this.clienteConectados = new ArrayList<>();

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
        //salidaStream=new DataOutputStream(new DataOutputStream(socket.getOutputStream()));
        //out = new ObjectOutputStream(socket.getOutputStream());
        //in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        //entrada = new DataInputStream(socket.getInputStream());
        entrada=new ObjectInputStream(socket.getInputStream());
        salida.writeObject(new Mensaje(hostName, CommunicationType.MESSAGE));
        TimeUnit.MILLISECONDS.sleep(500); // Esperar 1 segundo antes de la próxima actualización
        salida.flush();

        // Iniciar hilos para leer mensajes y recibir archivos
        executorService.submit(new ReadMessages(entrada));
        executorService.submit(new Servidor(9091)); // Suponiendo que este es el puerto para recibir archivos
    }

    public void conexionAutomatica() throws IOException, InterruptedException {
        DatagramSocket socket = new DatagramSocket(9092);
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
       
        socket.receive(packet); // Recibe el mensaje de broadcast
        String received = new String(packet.getData(), 0, packet.getLength());
        String[] argsServer = received.split("\\[|]");
        SERVER_ADDRESS = argsServer[1];
        SERVER_PORT = Integer.parseInt(argsServer[3]);
        setConexion(SERVER_ADDRESS, SERVER_PORT);

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

    /*private  void sendFile(File file,String message) {

        try  {
            Socket socket=new Socket(SERVER_ADDRESS,9090);
            //DataOutputStream salida=new DataOutputStream(socket.getOutputStream());
            ObjectOutputStream salida=new ObjectOutputStream(socket.getOutputStream());

            FileInputStream fileInputStream = new FileInputStream(file);
            String[] parts = message.split(" ", 3);
            String recipientNick = parts[1];
            String filePath = parts[2];


            transferencesObserver.addTransference("send", recipientNick, recipientNick,filePath.substring(filePath.lastIndexOf(File.separator)));

            salida.writeObject(new Mensaje("enviando",CommunicationType.MESSAGE));
            salida.flush();
            //TimeUnit.SECONDS.sleep(1);
            salida.writeObject(new Mensaje(message,CommunicationType.FILE));
            salida.flush();
            //TimeUnit.MILLISECONDS.sleep(500); // Esperar 1 segundo antes de la próxima actualización
            salida.writeUTF(file.getName());
            salida.writeLong(file.length());
            byte[] buffer = new byte[50 * 1024 * 1024];  // 50 MB
            //byte[] buffer = new byte[1024*4];
            int bytesRead;
            long totalBytesReaded = 0;
            long totalFileSize=file.length();
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {

                salida.write(buffer, 0, bytesRead);
                salida.flush();
                totalBytesReaded+=bytesRead;

                //transferencesObserver.updateTransference("sending", recipientNick, (int)((totalBytesReaded * 100) / totalFileSize));
                transferencesObserver.updateTransference("send", recipientNick, (int)((totalBytesReaded * 100) / totalFileSize));

            }
            if (socket.isConnected()&&entrada.readUTF().equals("ok")){

            }
            salida.flush();
            System.out.println("Archivo Enviado: " + file.getName());
            TimeUnit.SECONDS.sleep(10);
            salida.writeUTF("termino");
            TimeUnit.SECONDS.sleep(5);
            socket.close();


        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error sending file: " + e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
*/



    public void enviarMensaje(String mensaje) throws IOException {
        salida.writeObject(new Mensaje(mensaje,CommunicationType.MESSAGE));
    }

    // Método para actualizar la interfaz de usuario con el nuevo mensaje
    public void actualizarUIConMensaje(String message) {
        Platform.runLater(() -> chatArea.appendText(message + "\n"));
    }

    // Método para actualizar la lista de clientes conectados en la UI

    public String getMsj() {
        return this.msj;
    }

    public void resetMsj() {
        this.msj=null;
    }


    public void addObserver(Observer observer) {
        observers.add(observer);
    }
    public void addHostOserver(HostsObserver observer){
        hostsObservers.add(observer);
    }

    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    // Notificar a todos los observadores
    private void notifyObservers() {
        for (Observer observer : observers) {
            if (msj != null) {
                observer.updateMessaje(msj);  // Notifica a los observadores con el nuevo mensaje
            }
        }
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



    /*private void updateClientes(String message) {
        this.msj = message;
        for (Observer observer : observers) {
            observer.updateClientsList(clienteConectados);
        } // Notificar a los observadores que hay un nuevo mensaje
    }*/

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


        private void cleanUp() {
            try {
                if (entrada != null) {
                    entrada.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                System.out.println("Error al cerrar recursos: " + e.getMessage());
            }
        }
    }

    // Servidor que recibe los archivos en un puerto diferente
     class Servidor implements Runnable {
        private int port;

        public Servidor(int port) {
            this.port = port;
        }

        @Override
        public void run() {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("Servidor escuchando en el puerto: " + port);
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Conexión aceptada de un cliente.");
                    FileTransferManager transferManager =new FileTransferManager(transferencesObserver);
                    new Thread(() -> transferManager.receiveFiles(clientSocket)).start();
                }
            } catch (IOException e) {
                System.err.println("Error en el servidor: " + e.getMessage());
            }
        }
    }

    private  void receiveFiles(Socket socket) {
        try (ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream())) {
            String recipientNick = entrada.readUTF();
            String fileName = entrada.readUTF(); // Leer nombre del archivo
            long fileSize = entrada.readLong();  // Leer tamaño del archivo
            //transferencesObserver.addTransference("receive", recipientNick, recipientNick,fileName,this);
            TimeUnit.MILLISECONDS.sleep(300); // Esperar 1 segundo antes de la próxima actualización
            try (FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
                //byte[] buffer = new byte[4096];
                byte[] buffer = new byte[50 * 1024 * 1024];  // 50 MB

                int bytesRead;
                long totalBytesRead = 0;

                while (totalBytesRead < fileSize) {
                    bytesRead = entrada.read(buffer);
                    if (bytesRead == -1) break;
                    fileOutputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    transferencesObserver.updateTransference(FileTransferState.RECEIVING, recipientNick, (int)((totalBytesRead * 100) / fileSize));

                }

                System.out.println("Archivo recibido: " + fileName);
            }
            socket.close();
        } catch (IOException e) {
            System.err.println("Error durante la recepción de archivos: " + e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }




}
