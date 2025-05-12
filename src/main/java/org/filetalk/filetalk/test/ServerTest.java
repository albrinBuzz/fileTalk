package org.filetalk.filetalk.test;

import org.filetalk.filetalk.server.Server;

import java.io.IOException;

public class ServerTest {
    public static void main(String[] args) throws Exception {
        System.out.println("Prueba");
        Server server=Server.getInstance();

        server.starServerCLI();
    }
}
