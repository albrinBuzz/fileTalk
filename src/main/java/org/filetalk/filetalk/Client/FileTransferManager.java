package org.filetalk.filetalk.Client;

import org.filetalk.filetalk.model.Observers.TransferencesObserver;
import org.filetalk.filetalk.server.ClientHandler;
import org.filetalk.filetalk.shared.CommunicationType;
import org.filetalk.filetalk.shared.FileTransferState;
import org.filetalk.filetalk.shared.Mensaje;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class FileTransferManager {
    private volatile boolean running = true;
    private volatile boolean paused = false;
    private final Object pauseLock = new Object();

    private final Logger LOGGER = LoggerFactory.getLogger(FileTransferManager.class);


    private TransferencesObserver transferencesObserver;

    public FileTransferManager(TransferencesObserver transferencesObserver) {
        this.transferencesObserver = transferencesObserver;
    }

    public   void sendFile(File file, String message, String SERVER_ADDRESS) {
        try  {
            Socket socket=new Socket(SERVER_ADDRESS,9090);
            //DataOutputStream salida=new DataOutputStream(socket.getOutputStream());
            ObjectOutputStream salida=new ObjectOutputStream(socket.getOutputStream());

            FileInputStream fileInputStream = new FileInputStream(file);
            String[] parts = message.split(" ", 3);
            String recipientNick = parts[1];
            String filePath = parts[2];


            transferencesObserver.addTransference("send", recipientNick, recipientNick,filePath.substring(filePath.lastIndexOf(File.separator)),this);

            salida.writeObject(new Mensaje("enviando", CommunicationType.MESSAGE));
            salida.flush();
            //TimeUnit.SECONDS.sleep(1);
            salida.writeObject(new Mensaje(message,CommunicationType.FILE));
            salida.flush();
            //TimeUnit.MILLISECONDS.sleep(500); // Esperar 1 segundo antes de la próxima actualización
            salida.writeUTF(file.getName());
            salida.writeLong(file.length());
            byte[] buffer = new byte[10 * 1024 * 1024];  // 50 MB
            //byte[] buffer = new byte[1024*4];
            int bytesRead;
            long totalBytesReaded = 0;
            long totalFileSize=file.length();

            while (totalBytesReaded<totalFileSize) {
                    //totalBytesReaded<totalFileSize;

                synchronized (pauseLock) {

                    if (paused){
                        logInfo("pausando el envio");
                        pauseLock.wait();

                    }else {
                        bytesRead = fileInputStream.read(buffer);
                        salida.write(buffer, 0, bytesRead);
                        salida.flush();
                        totalBytesReaded += bytesRead;
                        //transferencesObserver.updateTransference("sending", recipientNick, (int)((totalBytesReaded * 100) / totalFileSize));
                        double totalMB = totalBytesReaded / 1_048_576.0;  // Convertir bytes a MB
                        transferencesObserver.updateTransference(FileTransferState.SENDING, recipientNick, (int) ((totalBytesReaded * 100) / totalFileSize));
                        logInfo("Bytes leido " + bytesRead/1_048_576.0 + " MB");
                        logInfo("Enviando " + totalMB + " MB enviados.");

                    }
                }
            }
            /*if (socket.isConnected()&&entrada.readUTF().equals("ok")){

            }*/
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



    public   void receiveFiles(Socket socket) {
        try (ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream())) {
            String recipientNick = entrada.readUTF();
            String fileName = entrada.readUTF(); // Leer nombre del archivo
            long fileSize = entrada.readLong();  // Leer tamaño del archivo
            transferencesObserver.addTransference("receive", recipientNick, recipientNick,fileName,this);
            TimeUnit.MILLISECONDS.sleep(300); // Esperar 1 segundo antes de la próxima actualización
            try (FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
                //byte[] buffer = new byte[4096];
                byte[] buffer = new byte[10 * 1024 * 1024];  // 50 MB

                int bytesRead;
                long totalBytesRead = 0;

                while (totalBytesRead < fileSize) {
                    bytesRead = entrada.read(buffer);
                    if (bytesRead == -1) break;
                    fileOutputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    double totalMB = totalBytesRead / 1_048_576.0;
                    transferencesObserver.updateTransference(FileTransferState.RECEIVING, recipientNick, (int)((totalBytesRead * 100) / fileSize));
                    logInfo("Recibiendo " + totalMB + " MB Recibidos.");
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

        logInfo("Deteniendo la tranferencias");
    }

    public void resume() {
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll(); // Unblocks thread

            logInfo("Renaudando  la tranferencias");
        }
    }
}
