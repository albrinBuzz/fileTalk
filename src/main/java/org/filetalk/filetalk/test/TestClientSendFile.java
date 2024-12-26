package org.filetalk.filetalk.test;

import org.filetalk.filetalk.Client.Client;

import java.io.File;
import java.io.IOException;

public class TestClientSendFile {

    public static void main(String[] args) throws IOException, InterruptedException {

        String serverAddress = "localhost";
        int serverPort = 9090;
        String filePath = "/home/cris/Descargas/sqlbrowserfx-fat.jar";
        File file = new File(filePath);
        Client client=new Client();
        client.setConexion("192.168.100.5",9090);



        //client.sendFile(file, "/file jose " + filePath, serverAddress);
        //client.handleFileTransfer("/file fedo /home/cris/Descargas/sqlbrowserfx-fat.jar");



    }
}
