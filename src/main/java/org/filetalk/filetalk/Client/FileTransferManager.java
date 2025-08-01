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

    public FileTransferManager(TransferenciaController transferenciaController) {
        this.configCliente=new ConfiguracionCliente();
        this.transferenciaController=transferenciaController;

    }

    public void sendFile(File file, String message, String SERVER_ADDRESS, int port) {
        try {
            //Logger.logInfo("Conectándose para enviar archivo: " + file.getName());

            // Establecer conexión con el servidor
            Socket socket = new Socket(SERVER_ADDRESS, port);
            //Logger.logInfo("Conectado al servidor para enviar archivo: " + file.getName());

            // Crear flujos de entrada y salida
            ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());
            salida.flush();  // Aseguramos que la salida esté limpia antes de escribir
            //Logger.logInfo("Flujo de salida preparado.");

            ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
            //Logger.logInfo("Flujo de entrada preparado.");

            // Preparar la información del archivo
            FileInputStream fileInputStream = new FileInputStream(file);
            String[] parts = message.split(" ", 3);
            String recipientNick = parts[1];
            String filePath = parts[2];
            long length = file.length();
            

            // Registrar la transferencia
            String idTransfe = transferenciaController.addTransference(
                    FileTransferState.SENDING.name(), recipientNick, recipientNick,
                    filePath.substring(filePath.lastIndexOf(File.separator)), this
            );
            //Logger.logInfo("Transferencia registrada con ID: " + idTransfe);

            // Enviar mensaje de inicio de transferencia
            salida.writeObject(new Mensaje("Enviando", CommunicationType.MESSAGE));
            salida.flush();
            //Logger.logInfo("Mensaje de inicio de transferencia enviado.");

            // Enviar la información del archivo al servidor
            salida.writeObject(new FileDirectoryCommunication(file.getName(), length, recipientNick));
            salida.flush();
            //Logger.logInfo("Información del archivo enviada al servidor.");

            // Esperar la respuesta del servidor para iniciar la transferencia
            Object object;
            while (true) {

                try {
                    //Logger.logInfo("esperado la respuesta del server");
                    object = entrada.readObject();

                    if (object instanceof FileHandshakeCommunication respuesta) {
                        if (respuesta.getAction() == FileHandshakeAction.START_TRANSFER) {
                            //Logger.logInfo("Iniciando la transferencia, listo para enviar.");
                            break; // Salir del bucle, respuesta esperada
                        }
                    } else if (object instanceof Mensaje mensaje) {
                        //Logger.logInfo("Mensaje recibido del servidor: " + mensaje.getContenido());
                    }

                } catch (ClassNotFoundException | IOException e) {
                    //Logger.logError("Error leyendo objeto del servidor: " + e.getMessage());
                    break;
                }
            }

            // Iniciar transferencia de datos
            byte[] buffer = new byte[100 * 1024 * 1024];  // 50 MB
            int bytesRead;
            long totalBytesReaded = 0;

            //Logger.logInfo("Comenzando la transferencia de datos...");

            while (totalBytesReaded < length) {
                synchronized (pauseLock) {
                    if (paused) {
                        //Logger.logInfo("Transferencia pausada, esperando reanudación...");
                        pauseLock.wait(); // Esperar si la transferencia está pausada
                    } else {
                        bytesRead = fileInputStream.read(buffer);
                        if (bytesRead == -1) break; // Fin del archivo

                        salida.write(buffer, 0, bytesRead);
                        salida.flush();
                        totalBytesReaded += bytesRead;

                        // Actualizar el progreso de la transferencia
                        int progress = (int) ((totalBytesReaded * 100) / length);
                        transferenciaController.updateProgress(FileTransferState.SENDING, idTransfe, progress);
                        //Logger.logInfo("Progreso de transferencia: " + progress + "%");
                    }
                }
            }

            // Finalizar la transferencia
            //Logger.logInfo("Archivo enviado correctamente: " + file.getName());

            // Cerrar conexiones
            salida.flush();
            socket.close();
            //Logger.logInfo("Conexión cerrada.");

        } catch (IOException e) {
            //Logger.logError("Error al enviar el archivo: " + e.getMessage());
        } catch (InterruptedException e) {
            //Logger.logError("Error de interrupción en el proceso de transferencia: " + e.getMessage());
        }
    }




    public   void receiveFiles(String SERVER_ADDRESS,String port,FileHandshakeCommunication handshakeCommunication) {
        try {

            var communication=handshakeCommunication.getFileInfo();
            String recipientNick = communication.getRecipient();
            String fileName = communication.getName(); // Leer nombre del archivo
            long fileSize = communication.getSize();  // Leer tamaño del archivo

            var idTrans= transferenciaController.addTransference(FileTransferState.RECEIVING.name(), recipientNick, recipientNick,fileName,this);


            Socket socket=new Socket(SERVER_ADDRESS, Integer.parseInt(port));

            ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());

            ObjectInputStream entrada=new ObjectInputStream(socket.getInputStream());


            salida.writeObject(new Mensaje(handshakeCommunication.getSessionId(), CommunicationType.MESSAGE));
            salida.flush();



            String rutaDescargas = configCliente.obtener("cliente.directorio_descargas");


            Object object;
            while (true) {
                try {
                    object = entrada.readObject();

                    if (object instanceof FileHandshakeCommunication respuesta) {

                        // Validamos que sea la respuesta esperada (ej. inicio de transferencia)
                        if (respuesta.getAction() == FileHandshakeAction.START_TRANSFER &&
                                respuesta.getSessionId().equals(handshakeCommunication.getSessionId())) {

                            break; // Salir del bucle, ya tienes la respuesta esperada
                        }

                    } else if (object instanceof Mensaje mensaje) {
                        //Logger.logInfo("Mensaje recibido: " + mensaje.getContenido());
                        // Puedes seguir esperando o tomar otra acción
                    }

                } catch (ClassNotFoundException | IOException e) {
                    //Logger.logError("Error leyendo objeto del servidor: " + e.getMessage());
                    break;
                }
            }

            try (FileOutputStream fileOutputStream = new FileOutputStream(rutaDescargas+fileName)) {
                byte[] buffer = new byte[100 * 1024 * 1024];  // 50 MB

                int bytesRead;
                long totalBytesRead = 0;

                while (totalBytesRead < fileSize) {
                    bytesRead = entrada.read(buffer);
                    if (bytesRead == -1) break;
                    fileOutputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;

                    transferenciaController.updateProgress(FileTransferState.RECEIVING,idTrans,(int)((totalBytesRead * 100) / fileSize));

                }


            }

            entrada.close();
            socket.close();
        } catch (IOException e) {
            System.err.println("Error durante la recepción de archivos: " + e.getMessage());
        }
    }


    public void stop() {
        running = false;

        resume();

    }

    public void pause() {
        // you may want to throw an IllegalStateException if !running
        paused = true;

    }

    public void resume() {
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll(); // Unblocks thread

        }
    }
}
