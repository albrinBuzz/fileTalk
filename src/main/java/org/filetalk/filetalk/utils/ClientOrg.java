package org.filetalk.filetalk.utils;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;

public class ClientOrg {


    private String SERVER_ADDRESS = "192.168.100.5";
    private  int SERVER_PORT = 9090;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private DataInputStream entrada;
    private DataOutputStream salida;
    private String msj;

    public ClientOrg(String SERVER_ADDRESS, int SERVER_PORT) throws IOException {
        this.SERVER_ADDRESS = SERVER_ADDRESS;
        this.SERVER_PORT=SERVER_PORT;
        socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        System.out.println("Conectado");
        String ipAddress = InetAddress.getLocalHost().getHostAddress();
        System.out.println("Dirección IP de la máquina: " + ipAddress);

        InetAddress localHost = InetAddress.getLocalHost();

        // Obtener el nombre de la máquina
        String hostname = localHost.getHostName();
        System.out.println("Nombre de la máquina: " + hostname);

        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        entrada=new DataInputStream(socket.getInputStream());
        salida=new DataOutputStream(socket.getOutputStream());

    }

    public ClientOrg() throws IOException {
        socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        entrada=new DataInputStream(socket.getInputStream());
        salida=new DataOutputStream(socket.getOutputStream());
        new Thread(new ReadMessages(in)).start();
    }

    // Thread to read messages from the server
    class ReadMessages implements Runnable {
        private final BufferedReader in;

        public ReadMessages(BufferedReader in) {
            this.in = in;
        }
        private static boolean isValidUTF8(byte[] input) {
            CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
            decoder.onMalformedInput(CodingErrorAction.REPORT);
            decoder.onUnmappableCharacter(CodingErrorAction.REPORT);

            try {
                decoder.decode(ByteBuffer.wrap(input));
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public void run() {
            try {
                String message;
                while ((message = entrada.readUTF()) != null) {
                    if (message.startsWith("/file")) {
                        //String fileName = entrada.readUTF();
                        receiveFile(entrada.readUTF(), entrada.readLong());
                    } else {
                        msj=message;
                    }
                }
            } catch (IOException e) {
                System.out.println("Error reading from server: " + e.getMessage());
            }
        }
    }

    public String getMsj() {
        //System.out.println(msj);
        return msj;
    }

    public void setMsj(String msj) {
        this.msj = msj;
    }

    public void resetMsj(){
        this.msj=null;
    }

    public   void handleFileTransfer(String usuario,String ruta) throws IOException {

        /*String[] messageSplit = message.split(" ");
        if (messageSplit.length < 3) {
            System.out.println("Usage: /file <RecipientNick> <FilePath>");
            return;
        }*/

        //String recipientNick = messageSplit[1];
        //String filePath = message.substring(message.lastIndexOf(" ") + 1).trim();
        File file = new File(ruta);

        if (!file.exists() || !file.isFile()) {
            System.out.println("Invalid file path");
            return;
        }

        salida.writeUTF("/file "+usuario+" "+ruta);
        sendFile(file);
    }

    private  void receiveFile(String fileName, long size) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
            byte[] buffer = new byte[100 * 1024 * 1024]; // 4 MB
            //byte[] buffer = new byte[1024*4];
            int bytesRead;
            long totalBytesRead = 0;
            //System.out.println("tamaño recibido "+size);
            while (totalBytesRead<size){
                bytesRead=entrada.read(buffer);
                fileOutputStream.write(buffer, 0, bytesRead);
                totalBytesRead+=bytesRead;
            }

            System.out.println("Archivo " + fileName + " recibido.");
        } catch (IOException e) {
            System.out.println("Error receiving file: " + e.getMessage());
        }
    }

    private  void sendFile(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            salida.writeUTF(file.getName());
            salida.writeLong(file.length());

            byte[] buffer = new byte[100 * 1024 * 1024]; // 100 MB
            //byte[] buffer = new byte[1024*4];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                salida.write(buffer, 0, bytesRead);
            }

            salida.flush();
            System.out.println("File sent: " + file.getName());
        } catch (IOException e) {
            System.out.println("Error sending file: " + e.getMessage());
        }
    }

    private  void sendFileUser( String filePath) {
        try {
            //out.println(message); // Comando para notificar al servidor
            File file = new File(filePath);
            //DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            FileInputStream fileInputStream = new FileInputStream(filePath);

            salida.writeUTF(file.getName());
            salida.writeLong(file.length());

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                salida.write(buffer, 0, bytesRead);
            }

        } catch (IOException e) {
            System.out.println("nose ");
            System.out.println("Error sending file: " + e.getMessage());
        }
    }


    public void enviarMensaje(String mensaje) throws IOException {
        salida.writeUTF(mensaje);
    }

}
