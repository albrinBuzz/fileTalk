package org.filetalk.filetalk.test;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Servidor {
    private static final int PUERTO = 1234;
    private static final int NUMERO_MAXIMO_CLIENTES = 8;
    private static ExecutorService poolDeHilos;
    private static AtomicInteger clientesConectados = new AtomicInteger(0); // Para llevar el conteo de clientes

    public static void main(String[] args) {
        poolDeHilos = Executors.newFixedThreadPool(NUMERO_MAXIMO_CLIENTES);
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Servidor iniciado en el puerto " + PUERTO);
            while (true) {
                // Si ya hay más de 8 clientes, rechazar nuevas conexiones
                if (clientesConectados.get() <= NUMERO_MAXIMO_CLIENTES) {

                    Socket socketCliente = serverSocket.accept();
                    System.out.println("Nuevo cliente conectado: " + socketCliente.getInetAddress().getHostAddress());
                    clientesConectados.incrementAndGet(); // Aumentar el conteo de clientes conectados
                    poolDeHilos.submit(new ClienteHandler(socketCliente));
                }
                else {
                    System.out.println("El servidor ha alcanzado el número máximo de clientes. No se aceptarán más conexiones.");
                }


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClienteHandler implements Runnable {
        private Socket socketCliente;

        public ClienteHandler(Socket socketCliente) {
            this.socketCliente = socketCliente;
        }

        @Override
        public void run() {
            System.out.println("Cliente aceptado desde: " + socketCliente.getInetAddress().toString());
            try {
                // Simulamos alguna interacción con el cliente
                Thread.sleep(5000); // Dormir 5 segundos por ejemplo

            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                // Después de manejar al cliente, reducir el número de clientes conectados
                clientesConectados.decrementAndGet();

                try {
                    socketCliente.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
