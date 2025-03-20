package org.filetalk.filetalk.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfiguracionServidor {

    private Properties propiedades;
    private String ruta;

    public ConfiguracionServidor() {

        this.ruta= System.getProperty("user.home")+"/config.properties";
        propiedades = new Properties();
        try (FileInputStream entrada = new FileInputStream(ruta)) {
            propiedades.load(entrada);
        } catch (IOException e) {
            e.printStackTrace();
            // Manejo de excepciones adecuado
        }
    }

    public String obtener(String clave) {
        return propiedades.getProperty(clave);
    }

    public int obtenerInt(String clave, int valorPorDefecto) {
        try {
            return Integer.parseInt(propiedades.getProperty(clave, String.valueOf(valorPorDefecto)));
        } catch (NumberFormatException e) {
            return valorPorDefecto;
        }
    }


}
