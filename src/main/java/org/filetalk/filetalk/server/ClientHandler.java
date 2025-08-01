package org.filetalk.filetalk.server;

import org.filetalk.filetalk.Client.ClientInfo;
import org.filetalk.filetalk.shared.*;
import org.slf4j.LoggerFactory;


import java.io.*;
import java.net.Socket;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ClientHandler implements Runnable {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ClientHandler.class);
    public final Socket clientSocket;
    public String nick;
    private ObjectInputStream entrada;
    private ObjectOutputStream salida;
    private String ip;
    private Server server;
    private ConfiguracionServidor config = new ConfiguracionServidor();
    // Crear el logger JDK
    //private static final // LOGGER = //.getLogger(ClientHandler.class.getName());


    public ClientHandler(Socket socket, Server server) {
        this.clientSocket = socket;
        this.ip = socket.getInetAddress().toString();
        this.server = server;


    }

    public String getNick() {
        return nick;
    }


    @Override
    public void run() {

        try {


            if (clientSocket.getInputStream() == null || clientSocket.getOutputStream() == null) {
                Logger.logInfo("INPUT CLIENTE VACIO");
                return;
            }

            // Crear el flujo de salida primero
            salida = new ObjectOutputStream(clientSocket.getOutputStream());
            salida.flush(); // Aseguramos que el flujo de salida esté limpio antes de escribir

            // Crear el flujo de entrada después
            entrada = new ObjectInputStream(clientSocket.getInputStream());

            // Leer el mensaje del cliente
            Mensaje mensaje = (Mensaje) entrada.readObject();

            // Solicita al servidor un nick único basado en el deseado
            nick = server.getUniqueNick(mensaje.getContenido());



            sendComunicacion(new Mensaje( "Conectado al servidor como: " + nick, CommunicationType.MESSAGE));

            int puerto=Integer.parseInt(config.obtener("cliente.puerto"));

            Server.clients.put(clientSocket, new ClientInfo(clientSocket, nick,puerto));

            //System.out.printf("[%s] has joined the chat%n", nick);
            //Logger.logInfo(nick);
            if (!nick.equals("enviando") && !nick.chars().allMatch(Character::isDigit)) {
                // solo se permite si el nick NO es "enviando" y NO es solo dígitos
                server.addClientUpdate(new ClientInfo(clientSocket, nick, puerto));
                server.updateClient(nick);
                //Logger.logInfo("[ " + nick + "] Se ha unido al Chat");
                server.broadcastMessage("[ " + nick + "] Se ha unido al Chat", this);
            }



            //. logInfo("Esperando mensajes");
            while (clientSocket.isConnected()) {
                //.logInfo("Leyendo mensajes mensajes");
                Object incoming = entrada.readObject();
                //Logger.logInfo("clase: "+incoming.getClass());
                Logger.logInfo(this.toString());
                if (incoming instanceof Communication communication) {

                    server.totalMessagesReceived.getAndIncrement();

                    //Logger.logInfo("entra en la comumicacion");
                    handleComunication(communication);
                } else if (incoming instanceof String s) {
                    Logger.logInfo("Se recibió un String inesperado: " + s);
                    // Puedes ignorar, o manejar según necesidad
                } else {
                    Logger.logInfo("Tipo de objeto inesperado: " + incoming.getClass());
                }
            }

        }  catch (EOFException e) {
            // El flujo llegó al final inesperadamente

            Logger.logInfo("Fin inesperado del flujo de datos. "+e.getMessage());
            //e.printStackTrace();
        } catch (StreamCorruptedException e) {
            // El flujo está dañado
            Logger.logInfo("El flujo de datos está dañado."+e.getMessage());
            //e.printStackTrace();
        } catch (IOException e) {
            // Error de entrada/salida
            Logger.logInfo("Error de I/O: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // La clase no fue encontrada al deserializar el objeto
            Logger.logInfo("Clase no encontrada: " + e.getMessage());
            //e.printStackTrace();
        }catch (Exception e){
            Logger.logInfo("Error:. "+e.getMessage());
        }
        finally {

            shutDown();
        }
    }

    private void handleComunication(Communication communication) throws IOException {
            //Logger.logInfo("EN LA COMUNICACION");
        if (communication.getType().equals(CommunicationType.MESSAGE)) {
            Mensaje mensaje=(Mensaje)communication;
            server.broadcastMessage("[" + nick + "] => " + mensaje.getContenido(), this);
            server.addMessageHistory("[" + nick + "] => " + mensaje.getContenido());
        } else if (communication.getType().equals(CommunicationType.FILE)) {
            FileDirectoryCommunication com=(FileDirectoryCommunication)communication;
            sendFileToClient(com);
        }
        else if (communication.getType().equals(CommunicationType.DIRECTORY)) {
            //Mensaje mensaje=(Mensaje)communication;
            FileDirectoryCommunication com=(FileDirectoryCommunication)communication;
            sendDirectoryToClient(com);
        }
        else if (communication.getType().equals(CommunicationType.DISCONNECT)) {
           shutDown();
        }

    }



    private ClientHandler findClientByIp(String ip) {
        for (ClientHandler client : server.getClientPool()) {
            if (client.nick==null){
                return null;
            }
            if (client.nick.equals(ip)) {
                return client;
            }
        }
        return null;
    }


    private void sendDirectoryToClient(FileDirectoryCommunication communication) throws IOException {
        String fileName = communication.getName();
        long fileSize = communication.getSize();
        String recipientNick = communication.getRecipient();
        int totalArchivos=communication.getTotalArchivos();
        //String filePath = parts[2];
        Logger.logInfo("cliente a encontrar "+recipientNick);
        ClientHandler recipient = findClientByIp(recipientNick);

        try  {

            var envio= new FileDirectoryCommunication(fileName,totalArchivos,recipientNick);


            String random= String.valueOf(new Random().nextInt(1, 1000));

            FileHandshakeCommunication request = new FileHandshakeCommunication(
                    FileHandshakeAction.SEND_REQUEST,
                    random,
                    envio
            );

            recipient.salida.writeObject(request);
            recipient.salida.flush();


            recipient = null;
            int intentos = 0;
            int maxIntentos = 30; // 30 * 1.2s = 36 segundos

            while (recipient == null && intentos < maxIntentos) {
                TimeUnit.MILLISECONDS.sleep(100);
                recipient = findClientByIp(random);
                intentos++;
            }

            if (recipient == null) {
                Logger.logInfo("No se pudo encontrar al cliente recipiente después de " + intentos + " intentos.");
                // manejar error o abortar
            } else {
                Logger.logInfo("Cliente recipiente encontrado: " + recipient);

            }


            FileHandshakeCommunication response = new FileHandshakeCommunication(
                    FileHandshakeAction.START_TRANSFER,
                    random,
                    envio
            );



            recipient.salida.writeObject(response);
            recipient.salida.flush();
            salida.writeObject(response);
            salida.flush();



            byte[] buffer = new byte[100 * 1024 * 1024];  // 50 MB

            int bytesRead;
            long totalBytesSent = 0;



            while (this.clientSocket.isConnected()) {

                Object object = entrada.readObject();
                if (object instanceof FileDirectoryCommunication archivo) {


                    String nombreArchivo = entrada.readUTF();
                    //String rutaArchivo=rutaCarpetaActual+ nombreArchivo;
                    recipient.salida.writeObject(archivo);
                    recipient.salida.flush();
                    recipient.salida.writeUTF(nombreArchivo);
                    recipient.salida.flush();

                    //crearDirectorios(rutaArchivo);
                    //String rutaDescargas = configCliente.obtener("cliente.directorio_descargas");

                    bytesRead = 0;
                    long totalBytesRead = 0;
                    fileSize = archivo.getSize();
                    Logger.logInfo(archivo.toString());
                    if (archivo.isDirectory() && archivo.getSize() == 0) {
                        Logger.logInfo("directorio vacio");
                        continue;
                    }

                    while (totalBytesRead < fileSize) {
                        bytesRead = entrada.read(buffer);
                        if (bytesRead == -1) break;

                        totalBytesRead += bytesRead;

                        recipient.salida.write(buffer, 0, bytesRead);
                        recipient.salida.flush();
                        totalBytesSent += bytesRead;

                    }


                }else if (object instanceof FileHandshakeCommunication respuesta) {

                    if (respuesta.getAction().equals(FileHandshakeAction.TRANSFER_DONE)){

                        FileHandshakeCommunication requestCom = new FileHandshakeCommunication(
                                FileHandshakeAction.TRANSFER_DONE
                        );

                        recipient.salida.writeObject(requestCom);
                        recipient.salida.flush();
                        break;

                    }
                }
            }







                    /*while ((bytesRead = entrada.read(buffer)) != -1) {
                        salida.write(buffer, 0, bytesRead);
                        salida.flush();
                        totalBytesSent += bytesRead;
                    }*/

            //System.out.printf("Enviados %d bytes a %s...%n", totalBytesSent, recipientNick);
            server.addBytes(totalBytesSent);
            server.updateBytes();
            salida.flush();
            //TimeUnit.MILLISECONDS.sleep(3500);
            //.logInfo("Archivo enviado correctamente a " + recipientNick);
            shutDown();
        } catch (IOException e) {
            Logger.logInfo("Error: "+e.getMessage());
            e.printStackTrace();
            //LOGGER.log(Level.SEVERE, "Error al enviar el archivo a: " + recipientNick, e);
            //LOGGER.error("Error al enviar el archivo a: {}  Error: {}",recipientNick,e.getMessage());
        } catch (InterruptedException e) {
            Logger.logInfo("error "+e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            recipient.shutDown();
        }

    }


        private void sendFileToClient(FileDirectoryCommunication communication) throws IOException {
            String fileName = communication.getName();
            long fileSize = communication.getSize();
            String recipientNick = communication.getRecipient();
            //String filePath = parts[2];
            Logger.logInfo("cliente a encontrar "+recipientNick);
            ClientHandler recipient = findClientByIp(recipientNick);

                try  {

                    var envio= new FileDirectoryCommunication(fileName,fileSize,recipientNick);

                    String random= String.valueOf(new Random().nextInt(1, 1000));

                    FileHandshakeCommunication request = new FileHandshakeCommunication(
                            FileHandshakeAction.SEND_REQUEST,
                            random,
                            envio
                    );

                    recipient.salida.writeObject(request);
                    recipient.salida.flush();


                    recipient = null;
                    int intentos = 0;
                    int maxIntentos = 30; // 30 * 1.2s = 36 segundos

                    while (recipient == null && intentos < maxIntentos) {
                        TimeUnit.MILLISECONDS.sleep(500);
                        recipient = findClientByIp(random);
                        intentos++;
                    }

                    if (recipient == null) {
                        Logger.logInfo("No se pudo encontrar al cliente recipiente después de " + intentos + " intentos.");
                        // manejar error o abortar
                    } else {
                        Logger.logInfo("Cliente recipiente encontrado: " + recipient);
                    }


                    FileHandshakeCommunication response = new FileHandshakeCommunication(
                            FileHandshakeAction.START_TRANSFER,
                            random,
                            envio
                    );



                    recipient.salida.writeObject(response);
                    recipient.salida.flush();
                    this.salida.writeObject(response);
                    this.salida.flush();


                    byte[] buffer = new byte[100 * 1024 * 1024];  // 50 MB

                    int bytesRead;
                    long totalBytesSent = 0;


                    while (totalBytesSent < fileSize) {
                        bytesRead = entrada.read(buffer);
                        if (bytesRead == -1) break;

                        recipient.salida.write(buffer, 0, bytesRead);
                        recipient.salida.flush();
                        totalBytesSent += bytesRead;
                        double totalMB = totalBytesSent / 1_048_576.0;
                        //logInfo("Reenviado " + totalMB + " MB Reenviados.");

                    }


                    /*while ((bytesRead = entrada.read(buffer)) != -1) {
                        salida.write(buffer, 0, bytesRead);
                        salida.flush();
                        totalBytesSent += bytesRead;
                    }*/

                    //System.out.printf("Enviados %d bytes a %s...%n", totalBytesSent, recipientNick);
                    //server.addBytes(totalBytesSent);
                    //server.updateBytes();
                    salida.flush();
                    TimeUnit.MILLISECONDS.sleep(3500);
                    //.logInfo("Archivo enviado correctamente a " + recipientNick);
                    shutDown();
                } catch (IOException e) {
                    //LOGGER.log(Level.SEVERE, "Error al enviar el archivo a: " + recipientNick, e);
                    //LOGGER.error("Error al enviar el archivo a: {}  Error: {}",recipientNick,e.getMessage());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

    }

    public void shutDown() {

        //.logInfo("shutDown");

        try {

            server.broadcastMessage(nick + " Se a desconectado", this);
            Server.clients.remove(clientSocket);
            server.getClientPool().remove(this);
            server.updateClient();
            System.out.printf("[%s] has left the chat.%n", nick);
            clientSocket.close();

            if (entrada!=null && salida!=null){
                entrada.close();
                salida.close();
            }



        } catch (IOException e) {
            Logger.logInfo("Error al cerrar conexión con el cliente: "+e.getMessage());
            //LOGGER.error("Error al cerrar conexión con el cliente: {}",e.getMessage());
        }
    }

    public void sendComunicacion(Communication communication){
        try {

            salida.writeObject(communication);

        } catch (IOException e) {
            //LOGGER.error("Error al enviar con el cliente: {}",e.getMessage());
            //.logInfo("Error al enviar con el cliente: "+e.getMessage());
        }

    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ClientHandler{");
        sb.append("ip='").append(ip).append('\'');
        sb.append(", nick='").append(nick).append('\'');
        sb.append('}');
        return sb.toString();
    }

    /*public void sendMessage(Object message) {
        try {

            salida.writeObject(message);

        } catch (IOException e) {
            LOGGER.error("Error al enviar con el cliente: {}",e.getMessage());
        }
    }*/
}
