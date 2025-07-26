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
    private String rutaCopia;
    private String rutaOriginal;
    private String carpeta;
    private String rutaCarpetaActual;
    private String rutaCarpetaDestino;
    private ObjectOutputStream out;
    private ObjectInputStream entrada;
    private ConfiguracionCliente configCliente;
    InputStream inputData;
    OutputStream outputData;
    private TransferencesObserver transferencesObserver;
    private String nick;
    private int totalArchivos;
    private int archivosEnviados;
    private TransferenciaController transferenciaController;
    public DirectoryTransferManager(TransferenciaController transferenciaController) {
        this.configCliente=new ConfiguracionCliente();
        rutaCopia= Paths.get("").toAbsolutePath().toString();
        rutaCopia=configCliente.obtener("cliente.directorio_descargas");
        rutaCarpetaActual=configCliente.obtener("cliente.directorio_descargas");
        //rutaCarpetaActual=Paths.get("").toAbsolutePath().toString();
        this.transferenciaController=transferenciaController;
        rutaCarpetaDestino=rutaCopia;
        this.transferencesObserver = transferencesObserver;



    }

    public void sendDirectory(File archivo, String SERVER_ADDRESS, int port,String recipient) throws IOException {
            rutaOriginal=archivo.getAbsolutePath();
            carpeta=archivo.getName();
            nick=SERVER_ADDRESS;
            totalArchivos=0;
            archivosEnviados=0;


            AtomicInteger totalArchivos = new AtomicInteger(0);

            archivosTotales(archivo,totalArchivos);
            this.totalArchivos=totalArchivos.get();

        // Imprimir el tiempo en consola


            Socket socket=new Socket(SERVER_ADDRESS,port);



            out= new ObjectOutputStream(socket.getOutputStream());
            entrada=new ObjectInputStream(socket.getInputStream());
        out.writeObject(new Mensaje("enviando", CommunicationType.MESSAGE));
        out.flush();

        out.writeObject(new FileDirectoryCommunication(archivo.getName(),totalArchivos.get(),recipient));
        out.flush();


            Logger.logInfo("corregir implementacion de la transferencia");

            //transferencesObserver.addTransference("send", SERVER_ADDRESS, SERVER_ADDRESS,archivo.getName(),this);
        String idTransfe=  transferenciaController.addTransference(FileTransferState.SENDING.name(), nick, nick,archivo.getName(),this);



        Object object;
        while (true) {
            try {
                object = entrada.readObject();

                if (object instanceof FileHandshakeCommunication respuesta) {

                    // Validamos que sea la respuesta esperada (ej. inicio de transferencia)
                    if (respuesta.getAction() == FileHandshakeAction.START_TRANSFER ) {

                        Logger.logInfo("Recibido START_TRANSFER para sesión: " + respuesta.getSessionId());
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


        enviarDirectorio(archivo,idTransfe);

            socket.close();

        }



        public void enviarDirectorio(File archivo,String idTransfe) throws IOException {

            File[] files = archivo.listFiles();

            if (files != null) {

                for (File file : files) {

                        if (file.isFile()) {

                        rutaCopia= file.getCanonicalPath()
                                .substring(file.getAbsolutePath().indexOf(carpeta));

                        rutaCarpetaActual= file.getAbsolutePath();

                        copy();
                            archivosEnviados++;
                            transferenciaController.updateProgress(FileTransferState.SENDING,idTransfe,(int) ((archivosEnviados * 100) / totalArchivos));
                            //transferencesObserver.updateTransference(FileTransferState.SENDING, nick, (int) ((archivosEnviados * 100) / totalArchivos));
                            //transferencesObserver.updateTransference(FileTransferState.SENDING, nick, (int) ((archivosEnviados * 100) / totalArchivos));


                            Logger.logInfo("%"+(archivosEnviados * 100) / totalArchivos);

                    }
                    // Si es un directorio, llamar recursivamente
                    else if (file.isDirectory()) {

                            enviarDirectorio(file,idTransfe);

                    }
                }

            } else {
                System.out.println("No se pudo acceder al contenido de: " + archivo.getAbsolutePath());
            }

        }


        public void reciveDirectory(String SERVER_ADDRESS, String port, FileHandshakeCommunication handshakeCommunication) throws IOException, ClassNotFoundException {

            Socket socket=new Socket(SERVER_ADDRESS, Integer.parseInt(port));

            ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());

            ObjectInputStream entrada=new ObjectInputStream(socket.getInputStream());

            Logger.logInfo("conectandose al servidor para recibir datos");

            salida.writeObject(new Mensaje(handshakeCommunication.getSessionId(), CommunicationType.MESSAGE));
            salida.flush();




            var communication=handshakeCommunication.getFileInfo();
            carpeta = communication.getName(); // Leer nombre del archivo
            totalArchivos=communication.getTotalArchivos();
            Logger.logInfo("recibiendo directorio: "+carpeta);

            Object object;
            while (true) {
                try {
                    object = entrada.readObject();

                    if (object instanceof FileHandshakeCommunication respuesta) {

                        // Validamos que sea la respuesta esperada (ej. inicio de transferencia)
                        if (respuesta.getAction() == FileHandshakeAction.START_TRANSFER &&
                                respuesta.getSessionId().equals(handshakeCommunication.getSessionId())) {

                            Logger.logInfo("Recibido START_TRANSFER para sesión: " + respuesta.getSessionId());
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
            //System.out.println(rutaCopia);
            Logger. logInfo("Ruta Copia: "+rutaCopia);
            Logger.logInfo("Ruta Actual: "+rutaCarpetaActual);
            //logInfo(carpeta);

            File file=new File(carpeta);
            file.mkdir();
            String recipientNick = communication.getRecipient();
            //transferencesObserver.addTransference("receive", recipientNick, recipientNick,carpeta,this);
            String idTransfe=   transferenciaController.addTransference(FileTransferState.RECEIVING.name(),recipientNick, recipientNick,carpeta,this);





            while (socket.isConnected()){

                   var obj=entrada.readObject();
                    if (obj instanceof FileDirectoryCommunication fileCom){
                        Logger.logInfo(fileCom.toString());
                        var nombreArchivo=fileCom.getName();
                        var fileSize=fileCom.getSize();


                        Logger.logInfo("nombre archivo: "+nombreArchivo);
                        String rutaArchivo=rutaCarpetaActual+ nombreArchivo;
                        Logger.logInfo("ruta archivo: "+rutaArchivo);
                        //FileDirectoryCommunication archivo= (FileDirectoryCommunication) entrada.readObject();

                        crearDirectorios(rutaArchivo);
                        String rutaDescargas = configCliente.obtener("cliente.directorio_descargas");
                        try (FileOutputStream fos = new FileOutputStream(rutaDescargas+nombreArchivo)) {

                            byte[] buffer = new byte[100 * 1024 * 1024];  // 50 MB

                            int bytesRead;
                            long totalBytesRead = 0;
                            //long fileSize = archivo.getSize();
                            while (totalBytesRead < fileSize) {

                                bytesRead = entrada.read(buffer);
                                if (bytesRead == -1) break;
                                fos.write(buffer, 0, bytesRead);
                                totalBytesRead += bytesRead;
                                double totalMB = totalBytesRead / 1_048_576.0;

                                //logInfo("Recibiendo " + totalMB + " MB Recibidos.");
                            }
                            archivosEnviados++;
                            transferenciaController.updateProgress(FileTransferState.SENDING,idTransfe,(int) ((archivosEnviados * 100) / totalArchivos));
                            //transferencesObserver.updateTransference(FileTransferState.RECEIVING, recipientNick, (int)((archivosEnviados * 100) / totalArchivos));

                            Logger.logInfo("%"+(archivosEnviados * 100) / totalArchivos);


                        } catch (IOException e) {

                            Logger.logInfo(e.getMessage());
                        }
                    }else if (obj instanceof Mensaje msj){
                        Logger.logInfo(msj.getContenido());
                    }


                }



            Logger.logInfo("Directorio Recibido "+communication.getName());



        }


        public void crearDirectorios(String archivo){
            try {

                     Path path = Paths.get(archivo);


                Path  directorioPadre = path.getParent();

                if (directorioPadre != null) {
                         Files.createDirectories(directorioPadre);

                }
            } catch (IOException e) {
                Logger.logInfo("Error al crear los directorios: " + e.getMessage());
            }

        }

        private void copy() throws IOException {

            String sourcePath = rutaCarpetaActual;
            Logger.logInfo("Ruta Copia: "+rutaCopia);



            FileInputStream fileInputStream = new FileInputStream(sourcePath);


            //transferencesObserver.addTransference("send", recipientNick, recipientNick,filePath.substring(filePath.lastIndexOf(File.separator)),this);

            //byte[] buffer = new byte[10 * 1024 * 1024];  // 50 MB
            byte[] buffer = new byte[100 * 1024 * 1024];  // 50 MB

            //byte[] buffer = new byte[1024*4];
            int bytesRead;
            long totalBytesReaded = 0;
            long totalFileSize=new File(sourcePath).length();
            File archivo=new File(sourcePath);

            if (rutaCopia == null || rutaCopia.isEmpty()) {
                throw new IOException("Nombre de archivo vacío o nulo");
            }

            if (rutaCopia.getBytes(StandardCharsets.UTF_8).length > 65535) {
                throw new IOException("Nombre de archivo demasiado largo para writeUTF");
            }

            //out.writeUTF(rutaCopia);
            //out.flush();
            //out.writeLong(archivo.length());
            /*out.writeUTF(rutaCopia);
            out.flush();
            out.writeLong(archivo.length());*/
            out.writeObject(new FileDirectoryCommunication(rutaCopia,archivo.length()));
            out.flush();

            Logger.logInfo("Copiando: "+sourcePath);
            while (totalBytesReaded<totalFileSize) {

                bytesRead = fileInputStream.read(buffer);
                out.write(buffer, 0, bytesRead);
                out.flush();
                totalBytesReaded += bytesRead;
                //transferencesObserver.updateTransference("sending", recipientNick, (int)((totalBytesReaded * 100) / totalFileSize));
                double totalMB = totalBytesReaded / 1_048_576.0;  // Convertir bytes a MB
               // transferencesObserver.updateTransference(FileTransferState.SENDING, recipientNick, (int) ((totalBytesReaded * 100) / totalFileSize));

            }

            Logger.logInfo("Copiado: "+sourcePath);
            out.flush();
            fileInputStream.close();


        }

    private void archivosTotales(File archivo, AtomicInteger totalArchivos ) throws IOException {

        File[] files = archivo.listFiles();

        // Si no es null, procesamos los archivos
        if (files != null) {
            // Usamos forEach para mayor claridad y eficiencia
            Arrays.stream(files).forEach(file -> {
                if (file.isFile()) {
                    totalArchivos.incrementAndGet(); // Incrementa de forma segura en entornos multihilo
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


    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }
}
