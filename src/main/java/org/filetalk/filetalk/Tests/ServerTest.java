package org.filetalk.filetalk.Tests;
import org.filetalk.filetalk.server.Server;

import java.io.IOException;

public class ServerTest {
    public static void main(String[] args) throws IOException {
        Server server=Server.getInstance();

        server.starServerCLI();
    }
}