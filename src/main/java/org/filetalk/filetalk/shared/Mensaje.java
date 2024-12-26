package org.filetalk.filetalk.shared;

import java.io.Serializable;

public class Mensaje extends Communication {

    private String contenido;

    public Mensaje( String contenido,CommunicationType type) {
        super(type);

        this.contenido = contenido;
    }


    public String getContenido() {
        return contenido;
    }

    @Override
    public String toString() {
        return " Mensaje: " + contenido;
    }
}
