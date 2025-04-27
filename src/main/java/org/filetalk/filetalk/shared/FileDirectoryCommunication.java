package org.filetalk.filetalk.shared;

import java.io.File;
import java.io.Serializable;

public class FileDirectoryCommunication extends Communication implements Serializable {

    private String name;            // Nombre del archivo o directorio
    private long size;              // Tamaño del archivo/directorio en bytes
    private boolean isDirectory;    // Indicador de si es un archivo o un directorio
    private int  totalArchivos;
    // Constructor para archivo
    public FileDirectoryCommunication(String name, long size) {
        super(CommunicationType.FILE);  // O puedes usar CommunicationType.DIRECTORY si es un directorio
        this.name = name;
        this.size = size;

        this.isDirectory = false;  // Es un archivo por defecto
    }

    // Constructor para directorio
    public FileDirectoryCommunication(String name,int totalArchivos) {
        super(CommunicationType.DIRECTORY);
        this.name = name;
        this.size = 0;             // Un directorio no tiene un tamaño específico
        this.isDirectory = true;
        this.totalArchivos=totalArchivos;
    }

    // Getter para el nombre del archivo/directorio
    public String getName() {
        return name;
    }

    // Getter para el tamaño
    public long getSize() {
        return size;
    }


    // Verificar si es un directorio
    public boolean isDirectory() {
        return isDirectory;
    }

    public int getTotalArchivos() {
        return totalArchivos;
    }


    // Representación en cadena (opcional)
    @Override
    public String toString() {
        return "FileDirectoryCommunication{" +
                "name='" + name + '\'' +
                ", size=" + size +
                ", isDirectory=" + isDirectory +
                '}';
    }
}
