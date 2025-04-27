package org.filetalk.filetalk.server;

import org.filetalk.filetalk.Client.ClientInfo;
import org.filetalk.filetalk.shared.Communication;
import org.filetalk.filetalk.shared.CommunicationType;
import org.filetalk.filetalk.shared.Logger;
import org.filetalk.filetalk.shared.Mensaje;


import java.io.*;
import java.net.Socket;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ClientHandler implements Runnable {
    public final Socket clientSocket;
    public String nick;
    private ObjectInputStream entrada;
    private ObjectOutputStream salida;
    private String ip;
    private Server server;
    private ConfiguracionServidor config = new ConfiguracionServidor();
    // Crear el logger JDK
    //private static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());


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

            if(clientSocket.getInputStream()==null||clientSocket.getOutputStream()==null) {

                return;
            }


            //System.out.println("Cliente aceptado desde: "+clientSocket.getInetAddress().toString());
            Logger.logInfo("Cliente aceptado desde: "+clientSocket.getInetAddress().toString());
            entrada = new ObjectInputStream(clientSocket.getInputStream());
            salida = new ObjectOutputStream(clientSocket.getOutputStream());

            Logger.logInfo("Conectando al Servidor");

            Mensaje mensaje=(Mensaje) entrada.readObject();

            nick=mensaje.getContenido();

            sendComunicacion(new Mensaje( "Conectado al servidor como: " + nick, CommunicationType.MESSAGE));

            int puerto=Integer.parseInt(config.obtener("cliente.puerto"));

            Server.clients.put(clientSocket, new ClientInfo(clientSocket, nick,puerto));

            server.addClientUpdate(new ClientInfo(clientSocket, nick,puerto));
            //System.out.printf("[%s] has joined the chat%n", nick);
            if (!nick.equals("enviando")){
                server.broadcastMessage("[ " + nick + "] Se ha unido al Chat", this);
            }

            server.updateClient(nick);
            Communication communication;
            Logger. logInfo("Esperando mensajes");
            while (clientSocket.isConnected()) {
                Logger.logInfo("Leyendo mensajes mensajes");
                communication =(Communication) entrada.readObject();
                if (communication != null) {
                        server.totalMessagesReceived.getAndIncrement();
                        System.out.printf("[%s] => %s%n", nick, mensaje.getContenido());
                        //handleMessage(mensaje.getContenido());
                        handleComunication(communication);
                }else {
                    Logger.logInfo("Mensaje nulo");
                }
            }

        }  catch (EOFException e) {
            // El flujo llegó al final inesperadamente

            Logger.logInfo("Fin inesperado del flujo de datos.");
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            // El flujo está dañado
            System.err.println("El flujo de datos está dañado."+e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            // Error de entrada/salida
            System.err.println("Error de I/O: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // La clase no fue encontrada al deserializar el objeto
            System.err.println("Clase no encontrada: " + e.getMessage());
            e.printStackTrace();
        }finally {

            shutDown();
        }
    }

    private void handleComunication(Communication communication) throws IOException {

        if (communication.getType().equals(CommunicationType.MESSAGE)) {
            Mensaje mensaje=(Mensaje)communication;
            server.broadcastMessage("[" + nick + "] => " + mensaje.getContenido(), this);
            server.addMessageHistory("[" + nick + "] => " + mensaje.getContenido());
        } else if (communication.getType().equals(CommunicationType.FILE)) {
            Mensaje mensaje=(Mensaje)communication;
            sendFileToClient(mensaje.getContenido());
        }
        else if (communication.getType().equals(CommunicationType.DIRECTORY)) {
            Mensaje mensaje=(Mensaje)communication;
            sendFileToClient(mensaje.getContenido());
        }
        else if (communication.getType().equals(CommunicationType.DISCONNECT)) {
           shutDown();
        }

    }



    private void changeNickname(String message) {
        String[] messageSplit = message.split(" ", 2);
        if (messageSplit.length == 2) {
            server.broadcastMessage(nick + " changed nickname to " + messageSplit[1], this);
            nick = messageSplit[1];
            //out.println("Nickname changed to " + nick + " successfully");
        } else {
            //out.println("Usage: /nick <NewNickname>");
        }
    }

    private void directMessage(String message) {
        String[] parts = message.split(" ", 3);
        if (parts.length == 3) {
            String recipientNick = parts[1];
            String dmMessage = parts[2];
            ClientHandler recipient = findClientByIp(recipientNick);
            if (recipient != null) {
                recipient.sendComunicacion(new Mensaje( nick + " -> " + dmMessage,CommunicationType.MESSAGE));
            } else {
                //out.println("User not found: " + recipientNick);
            }
        } else {
            //out.println("Usage: /dm <RecipientNick> <Message>");
        }
    }

    private ClientHandler findClientByIp(String ip) {
        for (ClientHandler client : server.getClientPool()) {
            if (client.ip.equals(ip)) {
                return client;
            }
        }
        return null;
    }

    private void sendFileToClient(String message) throws IOException {
        String[] parts = message.split(" ", 3);
        if (parts.length == 3) {
            String fileName = entrada.readUTF();
            long fileSize = entrada.readLong();
            String recipientNick = parts[1];
            //String filePath = parts[2];

            ClientHandler recipient = findClientByIp(recipientNick);


                Logger.logInfo("Enviando archivo a: " +recipientNick);
                Logger.logInfo("Ip: "+recipient.ip.substring(1));


            Logger.logInfo("Enviando archivo a: {}"+ recipientNick);


                try (Socket recipientSocket = new Socket(recipient.ip.substring(1), 9091);
                     ObjectOutputStream salida=new ObjectOutputStream(recipientSocket.getOutputStream())) {

                    salida.writeUTF(recipientNick);
                    salida.flush();
                    salida.writeUTF(fileName);
                    salida.flush();
                    salida.writeLong(fileSize);
                    salida.flush();

                    byte[] buffer = new byte[10 * 1024 * 1024];  // 50 MB

                    int bytesRead;
                    long totalBytesSent = 0;


                    while (totalBytesSent < fileSize) {
                        bytesRead = entrada.read(buffer);
                        if (bytesRead == -1) break;
                        salida.write(buffer, 0, bytesRead);
                        salida.flush();
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
                    server.addBytes(totalBytesSent);
                    server.updateBytes();
                    salida.flush();
                    //System.out.println("Archivo enviado correctamente a " + recipientNick);
                    shutDown();
                } catch (IOException e) {
                    //LOGGER.log(Level.SEVERE, "Error al enviar el archivo a: " + recipientNick, e);
                    //LOGGER.error("Error al enviar el archivo a: {}  Error: {}",recipientNick,e.getMessage());
                } finally {
                    shutDown();
                }
            } else {
                //out.println("Usuario no encontrado: " + recipientNick);

            }

    }

    public void shutDown() {

        Logger.logInfo("shutDown");

        try {

            server.broadcastMessage(nick + " Se a desconectado", this);
            Server.clients.remove(clientSocket);
            server.getClientPool().remove(this);
            server.updateClient();
            System.out.printf("[%s] has left the chat.%n", nick);
            clientSocket.close();
            entrada.close();
            salida.close();


        } catch (IOException e) {
            //LOGGER.error("Error al cerrar conexión con el cliente: {}",e.getMessage());
        }
    }

    public void sendComunicacion(Communication communication){
        try {

            salida.writeObject(communication);

        } catch (IOException e) {
            //LOGGER.error("Error al enviar con el cliente: {}",e.getMessage());
            Logger.logInfo("Error al enviar con el cliente: "+e.getMessage());
        }

    }



    /*public void sendMessage(Object message) {
        try {

            salida.writeObject(message);

        } catch (IOException e) {
            LOGGER.error("Error al enviar con el cliente: {}",e.getMessage());
        }
    }*/
}
