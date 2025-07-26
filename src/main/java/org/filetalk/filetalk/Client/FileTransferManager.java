package org.filetalk.filetalk.Client;

import org.filetalk.filetalk.controller.TransferenciaController;
import org.filetalk.filetalk.model.Observers.TransferencesObserver;
import org.filetalk.filetalk.models.Transferencia;
import org.filetalk.filetalk.shared.*;


import java.io.*;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class FileTransferManager implements TransferManager{
    private volatile boolean running = true;
    private volatile boolean paused = false;
    private final Object pauseLock = new Object();
    private ConfiguracionCliente configCliente;
    private TransferenciaController transferenciaController;
    private TransferencesObserver transferencesObserver;

    public FileTransferManager(TransferenciaController transferenciaController) {
        //this.transferencesObserver = transferencesObserver;
        this.configCliente=new ConfiguracionCliente();
        this.transferenciaController=transferenciaController;

    }

    public   void sendFile(File file, String message, String SERVER_ADDRESS, int port) {
        try  {
            Logger.logInfo("Conectandose al servidor");
            Socket socket=new Socket(SERVER_ADDRESS,port);


            ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());
            FileInputStream fileInputStream = new FileInputStream(file);
            String[] parts = message.split(" ", 3);
            String recipientNick = parts[1];
            String filePath = parts[2];
            long length=file.length();

           String idTransfe=  transferenciaController.addTransference(FileTransferState.SENDING.name(), recipientNick, recipientNick,filePath.substring(filePath.lastIndexOf(File.separator)),this);



            Logger.logInfo("transfrencia agregada al controlador");
            salida.writeObject(new Mensaje("enviando", CommunicationType.MESSAGE));
            salida.flush();


            salida.writeObject(new FileDirectoryCommunication(file.getName(),length,recipientNick));
            salida.flush();

            //TimeUnit.SECONDS.sleep(1);
            //salida.writeUTF(recipientNick);
            byte[] buffer = new byte[10 * 1024 * 1024];  // 50 MB
            //byte[] buffer = new byte[1024*4];
            int bytesRead;
            long totalBytesReaded = 0;
            //TimeUnit.SECONDS.sleep(1);
            while (totalBytesReaded< length) {

                synchronized (pauseLock) {

                    if (paused){
                        Logger. logInfo("pausando el envio");
                        pauseLock.wait();

                    }else {
                        bytesRead = fileInputStream.read(buffer);
                        salida.write(buffer, 0, bytesRead);
                        salida.flush();
                        totalBytesReaded += bytesRead;
                        //transferencesObserver.updateTransference("sending", recipientNick, (int)((totalBytesReaded * 100) / totalFileSize));
                        double totalMB = totalBytesReaded / 1_048_576.0;  // Convertir bytes a MB
                        transferenciaController.updateProgress(FileTransferState.SENDING,idTransfe,(int) ((totalBytesReaded * 100) / length));
                        //transferencesObserver.updateTransference(FileTransferState.SENDING, recipientNick, (int) ((totalBytesReaded * 100) / length));
                        //logInfo("Bytes leido " + bytesRead/1_048_576.0 + " MB");
                        //logInfo("Enviando " + totalMB + " MB enviados.");

                    }
                }
            }

            salida.flush();
            Logger.logInfo("Archivo Enviado: " + file.getName());



        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error sending file: " + e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }



    public   void receiveFiles(String SERVER_ADDRESS,String port,FileHandshakeCommunication handshakeCommunication) {
        try {

            var communication=handshakeCommunication.getFileInfo();
            String recipientNick = communication.getRecipient();
            String fileName = communication.getName(); // Leer nombre del archivo
            long fileSize = communication.getSize();  // Leer tamaño del archivo
            //transferencesObserver.addTransference("receive", recipientNick, recipientNick,fileName,this);

            var idTrans= transferenciaController.addTransference(FileTransferState.RECEIVING.name(), recipientNick, recipientNick,fileName,this);


            //TimeUnit.MILLISECONDS.sleep(300); // Esperar 1 segundo antes de la próxima actualización
            //TimeUnit.MILLISECONDS.sleep(1); // Esperar 1 segundo antes de la próxima actualización
            Socket socket=new Socket(SERVER_ADDRESS, Integer.parseInt(port));

            ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());

            ObjectInputStream entrada=new ObjectInputStream(socket.getInputStream());

            Logger.logInfo("conectandose al servidor para recibir datos");

            salida.writeObject(new Mensaje(handshakeCommunication.getSessionId(), CommunicationType.MESSAGE));
            salida.flush();


            //salida.writeObject(new FileDirectoryCommunication(file.getName(),length,recipientNick));
            //salida.flush();

            String rutaDescargas = configCliente.obtener("cliente.directorio_descargas");


// Esperar hasta recibir una respuesta válida
            Object object;
            while (true) {
                try {
                    object = entrada.readObject();

                    if (object instanceof FileHandshakeCommunication respuesta) {

                        // Validamos que sea la respuesta esperada (ej. inicio de transferencia)
                        if (respuesta.getAction() == FileHandshakeAction.START_TRANSFER &&
                                respuesta.getSessionId().equals(handshakeCommunication.getSessionId())) {

                            Logger.logInfo("Recibido START_TRANSFER para sesión: " + respuesta.getSessionId());
                            break; // Salir del bucle, ya tienes la respuesta esperada
                        }

                    } else if (object instanceof Mensaje mensaje) {
                        Logger.logInfo("Mensaje recibido: " + mensaje.getContenido());
                        // Puedes seguir esperando o tomar otra acción
                    }

                } catch (ClassNotFoundException | IOException e) {
                    Logger.logError("Error leyendo objeto del servidor: " + e.getMessage());
                    break;
                }
            }

            try (FileOutputStream fileOutputStream = new FileOutputStream(rutaDescargas+fileName)) {
                //byte[] buffer = new byte[4096];
                byte[] buffer = new byte[100 * 1024 * 1024];  // 50 MB

                int bytesRead;
                long totalBytesRead = 0;

                while (totalBytesRead < fileSize) {
                    bytesRead = entrada.read(buffer);
                    if (bytesRead == -1) break;
                    fileOutputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    double totalMB = totalBytesRead / 1_048_576.0;
                   // transferencesObserver.updateTransference(FileTransferState.RECEIVING, recipientNick, (int)((totalBytesRead * 100) / fileSize));
                    transferenciaController.updateProgress(FileTransferState.RECEIVING,idTrans,(int)((totalBytesRead * 100) / fileSize));
                    //logInfo("Recibiendo " + totalMB + " MB Recibidos.");
                }


                Logger.logInfo("Archivo recibido: " + fileName);
            }

            entrada.close();
            socket.close();
        } catch (IOException e) {
            System.err.println("Error durante la recepción de archivos: " + e.getMessage());
        }
    }


    public void stop() {
        running = false;
        // you might also want to interrupt() the Thread that is
        // running this Runnable, too, or perhaps call:

        resume();
        // to unblock
    }

    public void pause() {
        // you may want to throw an IllegalStateException if !running
        paused = true;

        Logger. logInfo("Deteniendo la tranferencias");
    }

    public void resume() {
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll(); // Unblocks thread

            Logger.logInfo("Renaudando  la tranferencias");
        }
    }
}
