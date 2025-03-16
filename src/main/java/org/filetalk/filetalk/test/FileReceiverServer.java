package org.filetalk.filetalk.test;

import java.io.*;
import java.net.*;

public class FileReceiverServer {

    private static final int PORT = 12345; // Puerto donde escuchará el servidor
    private static final String DESTINATION_PATH = "/home/cris/videos/video.mp4"; // Ruta donde guardarás el archivo recibido

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Esperando conexión en el puerto " + PORT + "...");
            try (Socket socket = serverSocket.accept()) {
                System.out.println("Conexión establecida con: " + socket.getInetAddress());

                try (InputStream inputStream = socket.getInputStream();
                     FileOutputStream fileOutputStream = new FileOutputStream(DESTINATION_PATH);
                     BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {

                    byte[] buffer = new byte[8192]; // Tamaño del buffer
                    int bytesRead;
                    long totalBytesReceived = 0;
                    long startTime = System.nanoTime(); // Inicia el tiempo para el cálculo de la velocidad

                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        bufferedOutputStream.write(buffer, 0, bytesRead); // Escribe el bloque en el archivo
                        totalBytesReceived += bytesRead; // Acumula el total de bytes recibidos

                        // Calcular el tiempo transcurrido
                        long elapsedTime = System.nanoTime() - startTime;
                        double elapsedTimeInSeconds = elapsedTime / 1_000_000_000.0; // Convertir a segundos

                        // Calcular la velocidad de transferencia
                        if (elapsedTimeInSeconds > 0) {
                            double transferSpeed = totalBytesReceived / elapsedTimeInSeconds; // Bytes por segundo
                            double transferSpeedKBps = transferSpeed / 1024; // Convertir a KBps
                            System.out.println("Velocidad de recepción: " + String.format("%.2f", transferSpeedKBps) + " KB/s");
                        }
                    }

                    System.out.println("Archivo recibido y guardado en: " + DESTINATION_PATH);
                } catch (IOException e) {
                    System.err.println("Error al recibir el archivo: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e.getMessage());
        }
    }
}
