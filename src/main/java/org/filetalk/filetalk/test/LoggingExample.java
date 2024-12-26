package org.filetalk.filetalk.test;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingExample {

    // Crear el logger
    private static Logger logger = LoggerFactory.getLogger(LoggingExample.class);

    public static void main(String[] args) {
        logger.debug("Debug log message");
        logger.info("Info log message");
        logger.error("Error log message");
    }

    // Método que genera logs de diferentes niveles
    public void performAction() {
        // Log de nivel DEBUG (no debería mostrarse si el nivel es superior a DEBUG)
        logger.debug("Este es un mensaje de DEBUG");

        // Log de nivel INFO
        logger.info("Este es un mensaje de INFO");

        // Log de nivel WARN
        logger.warn("Este es un mensaje de WARN");

        // Log de nivel ERROR
        logger.error("Este es un mensaje de ERROR");
    }
}
