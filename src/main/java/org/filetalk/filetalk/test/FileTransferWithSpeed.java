package org.filetalk.filetalk.test;

import java.io.*;
import java.net.*;
import java.text.DecimalFormat;

public class FileTransferWithSpeed {

    private static final int BUFFER_SIZE = 8192; // 8 KB por bloque

    public static void main(String[] args) {
        String filePath = "/home/cris/Vídeos/breathOfLife.mp4"; // Cambia esto a la ruta de tu archivo
        String targetAddress = "localhost"; // Dirección del receptor
        int targetPort = 12345; // Puerto del receptor

        try (Socket socket = new Socket(targetAddress, targetPort);
             FileInputStream fileInputStream = new FileInputStream(filePath);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
             OutputStream outputStream = socket.getOutputStream()) {

            long totalBytesSent = 0;
            long startTime = System.nanoTime(); // Comienza a contar el tiempo
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            DecimalFormat df = new DecimalFormat("#.##");

            while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead); // Enviar el bloque
                totalBytesSent += bytesRead; // Acumulamos los bytes enviados

                // Calcular el tiempo transcurrido
                long elapsedTime = System.nanoTime() - startTime;
                double elapsedTimeInSeconds = elapsedTime / 1_000_000_000.0; // Convertir a segundos

                // Calcular la velocidad de transferencia
                if (elapsedTimeInSeconds > 0) {
                    double transferSpeed = totalBytesSent / elapsedTimeInSeconds; // Bytes por segundo
                    double transferSpeedKBps = transferSpeed / 1024; // Convertir a KBps
                    System.out.println("Velocidad de transferencia: " + df.format(transferSpeedKBps) + " KB/s");
                }
            }

            System.out.println("Archivo enviado correctamente.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
