package org.filetalk.filetalk.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class ConfiguracionServidor {

    private Properties propiedades;
    private String ruta;

    public ConfiguracionServidor() {
        propiedades = new Properties();
        loadConfig();
    }

    private void loadConfig(){
        String home=System.getProperty("user.home");
        String separador=System.getProperty("file.separator");
        String rutaFileTalkConf=home+separador+".config"+separador+"fileTalk"+separador+"config.properties";
        ruta=rutaFileTalkConf;
        File file=new File(rutaFileTalkConf);
        if (!file.exists()){
            try {
                crearDirectorios(file.getAbsolutePath());
                file.createNewFile();
                setConfigs();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try (FileInputStream entrada = new FileInputStream(ruta)) {
            propiedades.load(entrada);
        } catch (IOException e) {
            e.printStackTrace();
            // Manejo de excepciones adecuado
        }

    }

    public void crearDirectorios(String archivo){
        try {

            Path path = Paths.get(archivo);


            Path  directorioPadre = path.getParent();

            if (directorioPadre != null) {
                Files.createDirectories(directorioPadre);

            }
        } catch (IOException e) {
            System.out.println("Error al crear los directorios: " + e.getMessage());
        }

    }

    public String obtener(String clave) {
        return propiedades.getProperty(clave);
    }

    public int obtenerInt(String clave, int valorPorDefecto) {
        try {
            return Integer.parseInt(propiedades.getProperty(clave, String.valueOf(valorPorDefecto)));
        } catch (NumberFormatException e) {
            return valorPorDefecto;
        }
    }

    private void setConfigs(){


        // Agregar las configuraciones al objeto
        propiedades.setProperty("servidor.nombre", "Servidor_Principal");
        propiedades.setProperty("servidor.descripcion", "'Servidor dedicado a la gestión de archivos y chats'");
        propiedades.setProperty("servidor.ubicacion", "Sala de Servidores 1");
        propiedades.setProperty("servidor.puerto", "8080");
        propiedades.setProperty("servidor.max_conexiones", "100");
        propiedades.setProperty("servidor.buffer_envio", "8192");
        propiedades.setProperty("servidor.broadcast", "9092");
        propiedades.setProperty("red.direccion_ip", "192.168.1.100");
        propiedades.setProperty("red.puerto", "9090");
        propiedades.setProperty("red.tiempo_espera", "5000");
        propiedades.setProperty("cliente.puerto", "9091");
        propiedades.setProperty("rendimiento.hilos_maximos", "200");
        propiedades.setProperty("rendimiento.tiempo_respuesta_max", "1000");
        propiedades.setProperty("rendimiento.cache_tamano", "1024");
        propiedades.setProperty("conexion.pool_tamano", "50");
        propiedades.setProperty("conexion.tiempo_espera_max", "30000");
        propiedades.setProperty("conexion.reintentos", "3");
        propiedades.setProperty("sesion.tiempo_expiracion", "3600");
        propiedades.setProperty("sesion.habilitar_persistencia", "true");
        propiedades.setProperty("sesion.max_sesiones", "1000");

        // Escribir las propiedades en un archivo
        try (FileOutputStream output = new FileOutputStream(ruta)) {
            propiedades.store(output, "Configuración del Servidor");
            System.out.println("Archivo de configuración creado exitosamente.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
