package org.filetalk.filetalk.test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.net.InetSocketAddress;
import java.util.Scanner;

public class NioClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 1234;

    public static void main(String[] args) {
        try {
            // Abrir un canal de socket y conectarse al servidor
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(SERVER_ADDRESS, SERVER_PORT));
            socketChannel.configureBlocking(false);

            // Crear un selector y registrar el canal con él para operaciones de lectura
            Selector selector = Selector.open();
            socketChannel.register(selector, SelectionKey.OP_READ);

            // Crear un buffer para enviar datos
            ByteBuffer buffer = ByteBuffer.allocate(256);
            Scanner scanner = new Scanner(System.in);

            while (true) {
                // Leer una línea de entrada del usuario
                System.out.print("Ingrese un mensaje: ");
                String message = scanner.nextLine();

                // Enviar el mensaje al servidor
                buffer.clear();
                buffer.put(message.getBytes());
                buffer.flip();
                socketChannel.write(buffer);

                // Esperar por eventos de lectura
                selector.select();

                // Procesar los eventos disponibles
                for (SelectionKey key : selector.selectedKeys()) {
                    if (key.isReadable()) {
                        // Leer la respuesta del servidor
                        buffer.clear();
                        int bytesRead = socketChannel.read(buffer);
                        if (bytesRead == -1) {
                            System.out.println("Servidor cerrado la conexión.");
                            socketChannel.close();
                            return;
                        }
                        buffer.flip();
                        byte[] data = new byte[bytesRead];
                        buffer.get(data);
                        System.out.println("Respuesta del servidor: " + new String(data));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
