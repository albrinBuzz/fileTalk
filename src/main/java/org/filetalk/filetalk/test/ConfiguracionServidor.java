package org.filetalk.filetalk.test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


public class ConfiguracionServidor {

    public static void main(String[] args) {
        Properties propiedades = new Properties();

        try (FileInputStream entrada = new FileInputStream("./data/config.properties")) {
            // Cargar el archivo de propiedades
            propiedades.load(entrada);

            // Configuración del Servidor
            String nombreServidor = propiedades.getProperty("servidor.nombre");
            String descripcionServidor = propiedades.getProperty("servidor.descripcion");
            String ubicacionServidor = propiedades.getProperty("servidor.ubicacion");
            int puertoServidor = Integer.parseInt(propiedades.getProperty("servidor.puerto"));
            int maxConexiones = Integer.parseInt(propiedades.getProperty("servidor.max_conexiones"));
            int bufferEnvio = Integer.parseInt(propiedades.getProperty("servidor.buffer_envio"));

            // Configuración de Red
            String ipDireccion = propiedades.getProperty("red.direccion_ip");
            int redPuerto = Integer.parseInt(propiedades.getProperty("red.puerto"));
            int tiempoEspera = Integer.parseInt(propiedades.getProperty("red.tiempo_espera"));

            // Configuración de Rendimiento
            int hilosMaximos = Integer.parseInt(propiedades.getProperty("rendimiento.hilos_maximos"));
            int tiempoRespuestaMax = Integer.parseInt(propiedades.getProperty("rendimiento.tiempo_respuesta_max"));
            int cacheTamano = Integer.parseInt(propiedades.getProperty("rendimiento.cache_tamano"));

            // Mostrar los valores leídos
            System.out.println("Configuración del Servidor:");
            System.out.println("Nombre: " + nombreServidor);
            System.out.println("Descripción: " + descripcionServidor);
            System.out.println("Ubicación: " + ubicacionServidor);
            System.out.println("Puerto: " + puertoServidor);
            System.out.println("Máximo de Conexiones: " + maxConexiones);
            System.out.println("Tamaño del Buffer de Envío (bytes): " + bufferEnvio);

            System.out.println("\nConfiguración de Red:");
            System.out.println("Dirección IP: " + ipDireccion);
            System.out.println("Puerto: " + redPuerto);
            System.out.println("Tiempo de Espera (ms): " + tiempoEspera);

            System.out.println("\nConfiguración de Rendimiento:");
            System.out.println("Hilos Máximos: " + hilosMaximos);
            System.out.println("Tiempo de Respuesta Máximo (ms): " + tiempoRespuestaMax);
            System.out.println("Tamaño de la Caché (MB): " + cacheTamano);

            // Aquí puedes agregar la lógica para inicializar y configurar tu servidor
            // utilizando los valores leídos del archivo de configuración.

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
