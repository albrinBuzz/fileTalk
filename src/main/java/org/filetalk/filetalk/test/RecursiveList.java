package org.filetalk.filetalk.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class RecursiveList {

    static String rutaCopia="/home/cris/copias/Descargas";
    //static String rutaOriginal="/home/cris/Magic-IoC-Container";
    static String carpeta="Descargas";
    static String rutaCarpetaActual="/home/cris/Descargas";
    static String rutaCarpetaDestino=rutaCopia;
    static int totalArchivos=0;
    public static void main(String[] args) throws IOException {

        // Medir el tiempo de inicio
        long startTime = System.nanoTime();

        // Ruta del directorio de ejemplo
        File directorio = new File("/home/cris/");
        AtomicInteger totalArchivos = new AtomicInteger(0);
        //File directorio = new File("/home/cris/cc++");

        // Verificar si la ruta es un directorio y existe
        if (directorio.exists() && directorio.isDirectory()) {
            // Iniciar la recursión
            //listFiles(directorio);
            conteoRecursivo(directorio,totalArchivos);
        } else {
            System.out.println("El directorio no existe o no es válido.");
        }

        // Medir el tiempo de fin
        long endTime = System.nanoTime();

        // Calcular el tiempo transcurrido en segundos
        double durationInSeconds = (endTime - startTime) / 1_000_000_000.0;

        System.out.println("Tiempo de ejecución: " + durationInSeconds + " segundos");
        System.out.printf("Tiempo de ejecución: %.3f segundos%n", durationInSeconds);

    }





    static void conteoRecursivo(File archivo, AtomicInteger totalArchivos) {
        File[] files = archivo.listFiles();

        // Si no es null, procesamos los archivos
        if (files != null) {
            // Usamos forEach para mayor claridad y eficiencia
            Arrays.stream(files).forEach(file -> {
                if (file.isFile()) {
                    totalArchivos.incrementAndGet(); // Incrementa de forma segura en entornos multihilo
                } else if (file.isDirectory()) {
                    conteoRecursivo(file, totalArchivos); // Llamada recursiva si es un directorio
                }
            });
        } else {
            // Si no se puede acceder al contenido del directorio, mejor manejar el error
            System.err.println("No se pudo acceder al contenido de: " + archivo.getAbsolutePath());
        }
    }



    // Método recursivo para listar archivos y subdirectorios
    static void listFiles(File archivo) throws IOException {
        // Obtener la lista de archivos/directorios
        File[] files = archivo.listFiles();

        if (files != null) {
            for (File file : files) {

                if (file.isFile()) {

                    var directorioBase= file.getCanonicalPath()
                            .substring(file.getAbsolutePath().indexOf(carpeta));
                    var directorio=directorioBase.substring(directorioBase
                            .indexOf("/"));
                    rutaCarpetaDestino=rutaCopia+directorio;
                    rutaCarpetaActual= file.getAbsolutePath();
                    /*System.out.println("Archivo: "+ file.getName());
                    if (new File(rutaCarpetaActual+"/"+file.getName()).exists()){

                        copy(file.getName());
                    }else {
                        rutaCarpetaActual=rutaCarpetaActual.substring(0,rutaCarpetaActual.lastIndexOf("/"));
                        rutaCarpetaActual=rutaCarpetaActual.substring(0,rutaCarpetaActual.lastIndexOf("/"));
                        System.out.println("Ruta actual recortada-> "+rutaCarpetaActual);
                        copy(file.getName());
                    }*/

                    copy(file.getName());
                }
                // Si es un directorio, llamar recursivamente
                else if (file.isDirectory()) {

                    var directorioBase= file.getCanonicalPath()
                            .substring(file.getAbsolutePath().indexOf(carpeta));
                    var directorio=directorioBase.substring(directorioBase
                            .indexOf("/"));
                    //System.out.println("Ruta Actual: " +file.getCanonicalPath());
                    //System.out.println("Ruta Destino: " +rutaCopia+directorio);
                    rutaCarpetaDestino=rutaCopia+directorio;
                    rutaCarpetaActual= file.getAbsolutePath();


                    new File(rutaCarpetaDestino).mkdirs();
                    //System.out.println("Directorio: \n"+file.getAbsolutePath());

                    listFiles(file);

                }
            }
        } else {
            System.out.println("No se pudo acceder al contenido de: " + archivo.getAbsolutePath());
        }
    }

    static void copy(String archivo){
        // Rutas de origen y destino
        String sourcePath = rutaCarpetaActual;
        String destinationPath =  rutaCarpetaDestino;

        //System.out.println("Ruta actual: "+rutaCarpetaActual);
        //System.out.println("Ruta Destino: "+destinationPath);

        if(!new File(rutaCopia).exists()){
            //System.out.println("No existe"+rutaCopia);
            new File(rutaCopia).mkdirs();
        }

        try (FileInputStream inStream = new FileInputStream(sourcePath);
             FileOutputStream outStream = new FileOutputStream(destinationPath)) {

            // Buffer para almacenar los datos leídos
            byte[] buffer = new byte[5 * 1024 * 1024];  // 10 MB
            //byte[] buffer = new byte[1024];  // 10 MB
            int length;

            // Leer y escribir el archivo en bloques
            while ((length = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, length);
            }

            //System.out.println("Archivo copiado exitosamente!");

        } catch (IOException e) {
            System.out.println(e.getMessage());
            //e.printStackTrace();
        }

    }

    public void crearDirectorios(String archivo){
        try {
            // Crear el Path del archivo
            Path path = Paths.get(archivo);

            // Obtener el directorio padre (directorio del archivo)
            Path directorioPadre = path.getParent();

            // Crear los directorios de forma recursiva (si no existen)
            if (directorioPadre != null) {
                Files.createDirectories(directorioPadre);
                //System.out.println("Directorios creados: " + directorioPadre.toString());
            }
        } catch (IOException e) {
            System.out.println("Error al crear los directorios: " + e.getMessage());
        }

    }
}
