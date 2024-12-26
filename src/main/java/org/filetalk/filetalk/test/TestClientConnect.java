package org.filetalk.filetalk.test;

import org.filetalk.filetalk.Client.Client;

import java.io.IOException;

public class TestClientConnect {
    public static void main(String[] args) throws IOException, InterruptedException {
        Client client=new Client();
        client.setConexion("192.168.100.5",9090);





    }
}
