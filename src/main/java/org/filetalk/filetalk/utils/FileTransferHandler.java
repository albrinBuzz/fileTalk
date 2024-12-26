package org.filetalk.filetalk.utils;

import java.io.*;
import java.net.Socket;

public class FileTransferHandler {
    private volatile boolean running = true;
    private volatile boolean paused = false;
    private final Object pauseLock = new Object();



    public  void sendFile(Socket socket, String filePath) throws IOException {
        File file = new File(filePath);
        try (DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
             FileInputStream fileInputStream = new FileInputStream(file)) {

            // Enviar nombre y tamaño del archivo
            dataOutputStream.writeUTF(file.getName());
            dataOutputStream.writeLong(file.length());

            // Enviar archivo en bloques
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {

                synchronized (pauseLock) {

                    if (paused) {
                        pauseLock.wait();

                    } else {
                        dataOutputStream.write(buffer, 0, bytesRead);
                    }

                }
            }

            dataOutputStream.flush();
            System.out.println("File sent: " + filePath);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public  void receiveFile(Socket socket) throws IOException {
        try (DataInputStream dataInputStream = new DataInputStream(socket.getInputStream())) {

            // Recibir nombre y tamaño del archivo
            String fileName = dataInputStream.readUTF();
            long fileSize = dataInputStream.readLong();

            // Crear flujo de salida para guardar el archivo
            try (FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                long remaining = fileSize;

                while ((remaining > 0) && (bytesRead = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                    remaining -= bytesRead;
                }
                System.out.println("File received: " + fileName);
            }
        }
    }

    public void stop(){
        running=false;

    }
    public void setPaused(){
        paused=true;
    }
    public void resume(){
        paused=false;
        pauseLock.notify();
    }
}
