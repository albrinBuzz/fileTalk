package org.filetalk.filetalk.test;

import java.io.*;
import java.net.*;

public class Cliente {
    private static final String HOST = "localhost";
    private static final int PUERTO = 1234;

    public static void main(String[] args) {

        for (int i = 0; i < 8; i++) {
            conectarSocket();
        }


    }

    private static void conectarSocket(){

        try {

            Socket socket = new Socket(HOST, PUERTO);

            System.out.println("Conexión establecida con éxito.");

            socket.close();

        } catch (IOException e) {
            // Manejo de errores en caso de fallo de conexión
            System.err.println("Error al intentar conectar al servidor: " + e.getMessage());
            e.printStackTrace();
        }

    }
}
