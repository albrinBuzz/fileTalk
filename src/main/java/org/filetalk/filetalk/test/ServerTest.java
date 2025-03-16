package org.filetalk.filetalk.test;

import org.filetalk.filetalk.server.Server;

public class ServerTest {
    public static void main(String[] args) {
        Server server=Server.getInstance();

        server.starServerCLI();
    }
}
