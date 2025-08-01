package org.filetalk.filetalk.Client;

import org.filetalk.filetalk.controller.TransferenciaController;
import org.filetalk.filetalk.model.Observers.TransferencesObserver;
import org.filetalk.filetalk.shared.*;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class DirectoryTransferManager implements TransferManager{
    private volatile boolean running = true;
    private volatile boolean paused = false;
    private final Object pauseLock = new Object();
    private String rutaCopia;
    private String carpeta;
    private String rutaCarpetaActual;
    private ObjectOutputStream out;
    private ObjectInputStream entrada;
    private ConfiguracionCliente configCliente;
    private String nick;
    private int totalArchivos;
    private int archivosEnviados;
    private TransferenciaController transferenciaController;
    private String recipient;
    public DirectoryTransferManager(TransferenciaController transferenciaController) {
        this.configCliente=new ConfiguracionCliente();
        rutaCopia= Paths.get("").toAbsolutePath().toString();
        rutaCopia=configCliente.obtener("cliente.directorio_descargas");
        rutaCarpetaActual=configCliente.obtener("cliente.directorio_descargas");
        //rutaCarpetaActual=Paths.get("").toAbsolutePath().toString();
        this.transferenciaController=transferenciaController;





    }

    public void sendDirectory(File archivo, String SERVER_ADDRESS, int port,String recipient) throws IOException {
        carpeta=archivo.getName();
        nick=SERVER_ADDRESS;
        totalArchivos=0;
        archivosEnviados=0;
        this.recipient=recipient;


        AtomicInteger totalArchivos = new AtomicInteger(0);

        archivosTotales(archivo,totalArchivos);
        this.totalArchivos=totalArchivos.get();




       try(Socket socket=new Socket(SERVER_ADDRESS,port)) {


           out = new ObjectOutputStream(socket.getOutputStream());
           entrada = new ObjectInputStream(socket.getInputStream());
           out.writeObject(new Mensaje("enviando", CommunicationType.MESSAGE));
           out.flush();

           out.writeObject(new FileDirectoryCommunication(archivo.getName(), totalArchivos.get(), recipient));
           out.flush();

           String idTransfe = transferenciaController.addTransference(FileTransferState.SENDING.name(), nick, nick, archivo.getName(), this);


           Object object;
           while (true) {
               try {
                   object = entrada.readObject();

                   if (object instanceof FileHandshakeCommunication respuesta) {

                       // Validamos que sea la respuesta esperada (ej. inicio de transferencia)
                       if (respuesta.getAction() == FileHandshakeAction.START_TRANSFER) {

                           break; // Salir del bucle, ya tienes la respuesta esperada
                       }

                   } else if (object instanceof Mensaje mensaje) {
                       Logger.logInfo("Mensaje recibido: " + mensaje.getContenido());
                       // Puedes seguir esperando o tomar otra acción
                   }

               } catch (ClassNotFoundException | IOException e) {
                   Logger.logError("Error leyendo objeto del servidor: " + e.getMessage());
                   break;
               }
           }


           enviarDirectorio(archivo, idTransfe);

           FileHandshakeCommunication request = new FileHandshakeCommunication(
                   FileHandshakeAction.TRANSFER_DONE
           );

           out.writeObject(request);
           out.flush();

       }catch (Exception e){
           Logger.logInfo("error :"+e.getMessage());

       }





    }



    public void enviarDirectorio(File archivo,String idTransfe) throws IOException, InterruptedException {

        File[] files = archivo.listFiles();

        Logger.logInfo(Arrays.toString(files));


        if (files.length == 0) {

            rutaCopia = archivo.getCanonicalPath()
                    .substring(archivo.getAbsolutePath().indexOf(carpeta));
            rutaCarpetaActual = archivo.getAbsolutePath();

            out.writeObject(new FileDirectoryCommunication(archivo.getName(),0,recipient)); // archivo vacío y es un directorio
            out.flush();

            out.writeUTF(rutaCopia); // enviar ruta
            out.flush();

        }

        for (File file : files) {

            if (file.isFile()) {

                rutaCopia = file.getCanonicalPath()
                        .substring(file.getAbsolutePath().indexOf(carpeta));

                rutaCarpetaActual = file.getAbsolutePath();

                copy();
                archivosEnviados++;
                transferenciaController.updateProgress(FileTransferState.SENDING, idTransfe, (int) ((archivosEnviados * 100) / totalArchivos));

            }

            else if (file.isDirectory()) {

                enviarDirectorio(file, idTransfe);

            }
        }

    }


    public void reciveDirectory(String SERVER_ADDRESS, String port, FileHandshakeCommunication handshakeCommunication) throws IOException, ClassNotFoundException {

        Socket socket=new Socket(SERVER_ADDRESS, Integer.parseInt(port));

        ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());

        ObjectInputStream entrada=new ObjectInputStream(socket.getInputStream());

        salida.writeObject(new Mensaje(handshakeCommunication.getSessionId(), CommunicationType.MESSAGE));
        salida.flush();

        var communication=handshakeCommunication.getFileInfo();
        carpeta = communication.getName(); // Leer nombre del archivo
        totalArchivos=communication.getTotalArchivos();


        Object object;
        while (true) {
            try {
                object = entrada.readObject();

                if (object instanceof FileHandshakeCommunication respuesta) {

                    // Validamos que sea la respuesta esperada (ej. inicio de transferencia)
                    if (respuesta.getAction() == FileHandshakeAction.START_TRANSFER &&
                            respuesta.getSessionId().equals(handshakeCommunication.getSessionId())) {

                        break; // Salir del bucle, ya tienes la respuesta esperada
                    }

                } else if (object instanceof Mensaje mensaje) {
                    Logger.logInfo("Mensaje recibido: " + mensaje.getContenido());
                    // Puedes seguir esperando o tomar otra acción
                }

            } catch (ClassNotFoundException | IOException e) {
                Logger.logError("Error leyendo objeto del servidor: " + e.getMessage());
                break;
            }
        }

        rutaCopia+=carpeta;


        File file=new File(carpeta);
        file.mkdir();
        String recipientNick = communication.getRecipient();
        String idTransfe=   transferenciaController.addTransference(FileTransferState.RECEIVING.name(),recipientNick, recipientNick,carpeta,this);





        while (true){

            object = entrada.readObject();
            if (object instanceof FileDirectoryCommunication archivo) {


                String nombreArchivo = entrada.readUTF();


            String rutaArchivo=rutaCarpetaActual+ nombreArchivo;



            if (archivo.isDirectory()&&archivo.getSize()==0){
                new File(rutaArchivo).mkdir();
                archivosEnviados++;
                transferenciaController.updateProgress(FileTransferState.SENDING,idTransfe,(int) ((archivosEnviados * 100) / totalArchivos));
                continue;
            }

                crearDirectorios(rutaArchivo);
                String rutaDescargas = configCliente.obtener("cliente.directorio_descargas");
                try (FileOutputStream fos = new FileOutputStream(rutaDescargas+nombreArchivo)) {

                    byte[] buffer = new byte[100 * 1024 * 1024];  // 50 MB

                    int bytesRead;
                    long totalBytesRead = 0;
                    long fileSize = archivo.getSize();



                    while (totalBytesRead < fileSize) {

                        bytesRead = entrada.read(buffer);
                        if (bytesRead == -1) break;
                        fos.write(buffer, 0, bytesRead);
                        totalBytesRead += bytesRead;


                    }
                    archivosEnviados++;
                    transferenciaController.updateProgress(FileTransferState.SENDING,idTransfe,(int) ((archivosEnviados * 100) / totalArchivos));




                } catch (IOException e) {

                    Logger.logInfo(e.getMessage());
                }



            }else if (object instanceof FileHandshakeCommunication respuesta) {

                if (respuesta.getAction().equals(FileHandshakeAction.TRANSFER_DONE)){

                    socket.close();
                    break;

                }
            }
        }






    }

    public void crearDirectorios(String archivo) {
        try {
            Path path = Paths.get(archivo);
            Path directorioPadre = path.getParent();



            if (directorioPadre != null) {
                if (Files.exists(directorioPadre)) {
                    if (!Files.isDirectory(directorioPadre)) {

                        Files.delete(directorioPadre);

                        Files.createDirectories(directorioPadre);

                    }
                } else {
                    Files.createDirectories(directorioPadre);

                }
            }

        } catch (IOException e) {
            Logger.logInfo(String.format(
                    "[crearDirectorios] ❗ IOException al crear directorios para: %s%n  - Mensaje: %s%n  - Clase excepción: %s",
                    archivo, e.getMessage(), e.getClass().getName()
            ));
            e.printStackTrace();
        } catch (Exception ex) {
            Logger.logInfo(String.format(
                    "[crearDirectorios] ❗ Excepción inesperada: %s%n  - Mensaje: %s%n  - Clase: %s",
                    archivo, ex.getMessage(), ex.getClass().getName()
            ));
            ex.printStackTrace();
        }
    }


    private void copy() throws IOException, InterruptedException {

        String sourcePath = rutaCarpetaActual;


        FileInputStream fileInputStream = new FileInputStream(sourcePath);


        byte[] buffer = new byte[100 * 1024 * 1024];  // 50 MB
        int bytesRead;
        long totalBytesReaded = 0;
        long totalFileSize=new File(sourcePath).length();
        File archivo=new File(sourcePath);
        out.writeObject(new FileDirectoryCommunication(archivo.getName(),archivo.length()));
        out.flush();

        out.writeUTF(rutaCopia);
        out.flush();

        while (totalBytesReaded<totalFileSize) {

            /*bytesRead = fileInputStream.read(buffer);
            out.write(buffer, 0, bytesRead);
            out.flush();
            totalBytesReaded += bytesRead;*/

            synchronized (pauseLock) {

                if (paused){
                    pauseLock.wait();

                }else {
                    bytesRead = fileInputStream.read(buffer);
                    out.write(buffer, 0, bytesRead);
                    out.flush();
                    totalBytesReaded += bytesRead;

                    //double totalMB = totalBytesReaded / 1_048_576.0;  // Convertir bytes a MB
                    //transferenciaController.updateProgress(FileTransferState.SENDING,idTransfe,(int) ((totalBytesReaded * 100) / length));


                }
            }


        }

        out.flush();
        fileInputStream.close();


    }

    private void archivosTotales(File archivo, AtomicInteger totalArchivos ) throws IOException {

        File[] files = archivo.listFiles();

        if (files != null) {

            Arrays.stream(files).forEach(file -> {
                if (file.isFile()) {
                    totalArchivos.incrementAndGet();
                } else if (file.isDirectory()) {
                    try {
                        archivosTotales(file, totalArchivos); // Llamada recursiva si es un directorio
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } else {
            // Si no se puede acceder al contenido del directorio, mejor manejar el error
            System.err.println("No se pudo acceder al contenido de: " + archivo.getAbsolutePath());
        }

    }


    public void stop() {
        running = false;

        resume();

    }

    public void pause() {
        // you may want to throw an IllegalStateException if !running
        paused = true;

    }

    public void resume() {
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll(); // Unblocks thread

        }
    }
}