package org.filetalk.filetalk.Client;

import java.io.Serializable;
import java.net.Socket;

public class ClientInfo implements Serializable {
    private String address;
    private long connectionTime;
    private String nick;
    public ClientInfo(Socket socket, String nick) {
        this.address = socket.getInetAddress().toString();
        this.connectionTime = System.currentTimeMillis();

        this.nick=nick;
    }

    public ClientInfo(String version, long tiempoConexion, String nick) {
        this.address = version;
        this.connectionTime = tiempoConexion;
        this.nick = nick;
    }

    public String getAddress() {
        return address;
    }

    public long getConnectionTime() {
        //return System.currentTimeMillis() - connectionTime;
        return connectionTime;
    }

    public String formatUptime() {
        long uptime = System.currentTimeMillis() - connectionTime;
        long seconds = (uptime / 1000) % 60;
        long minutes = (uptime / (1000 * 60)) % 60;
        long hours = (uptime / (1000 * 60 * 60)) % 24;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }


    public String getNick() {
        return this.nick;
    }
}
