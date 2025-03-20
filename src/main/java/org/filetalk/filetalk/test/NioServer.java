package org.filetalk.filetalk.test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.net.InetSocketAddress;
import java.util.Iterator;

public class NioServer {
    private static final int PORT = 1234;

    public static void main(String[] args) {
        try {
            // Abrir un canal de servidor y configurarlo en modo no bloqueante
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(PORT));
            serverSocketChannel.configureBlocking(false);

            // Abrir un selector
            Selector selector = Selector.open();

            // Registrar el canal de servidor con el selector para aceptar conexiones
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Servidor NIO iniciado en el puerto " + PORT);

            while (true) {
                // Esperar por eventos en los canales registrados
                selector.select();

                // Obtener las claves de selección que están listas para operaciones I/O
                Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();

                while (selectedKeys.hasNext()) {
                    SelectionKey key = selectedKeys.next();
                    selectedKeys.remove();

                    if (key.isAcceptable()) {
                        // Aceptar una nueva conexión de cliente
                        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                        SocketChannel clientChannel = serverChannel.accept();
                        clientChannel.configureBlocking(false);

                        // Registrar el canal del cliente con el selector para leer datos
                        clientChannel.register(selector, SelectionKey.OP_READ);
                        System.out.println("Nuevo cliente conectado: " + clientChannel.getRemoteAddress());
                    } else if (key.isReadable()) {
                        // Leer datos de un canal de cliente
                        SocketChannel clientChannel = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(256);
                        int bytesRead = clientChannel.read(buffer);

                        if (bytesRead == -1) {
                            // El cliente ha cerrado la conexión
                            System.out.println("Cliente desconectado: " + clientChannel.getRemoteAddress());
                            clientChannel.close();
                        } else {
                            // Procesar los datos leídos
                            buffer.flip();
                            byte[] data = new byte[bytesRead];
                            buffer.get(data);
                            String message = new String(data);
                            System.out.println("Mensaje recibido: " + message);

                            // Enviar una respuesta al cliente
                            buffer.clear();
                            buffer.put(("Echo: " + message).getBytes());
                            buffer.flip();
                            clientChannel.write(buffer);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
