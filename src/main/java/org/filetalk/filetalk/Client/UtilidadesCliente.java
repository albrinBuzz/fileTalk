package org.filetalk.filetalk.Client;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class UtilidadesCliente {



    public static boolean abrirDirectorioDescargas(String ruta) {
        File carpeta = new File(ruta);
        if (carpeta.exists() && carpeta.isDirectory()) {
            try {
                Desktop.getDesktop().open(carpeta);
                return true;
            } catch (IOException e) {
                System.err.println("No se pudo abrir el directorio: " + e.getMessage());
            }
        } else {
            System.out.println("La ruta no existe o no es un directorio válido.");
        }
        return false;
    }

}
