package org.filetalk.filetalk.shared;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    // Configuración de colores por defecto
    private static final String CLASS_COLOR = "\u001B[34m"; // Azul
    private static final String METHOD_COLOR = "\u001B[32m"; // Verde
    private static final String LINE_NUMBER_COLOR = "\u001B[33m"; // Amarillo
    private static final String MESSAGE_COLOR = "\u001B[35m"; // Magenta
    private static final String RESET_COLOR = "\u001B[0m"; // Restablecer el color

    // Niveles de log
    public enum LogLevel {
        DEBUG, INFO, WARN, ERROR;

        // Mapa de colores para cada nivel de log
        private static final String DEBUG_COLOR = "\u001B[36m"; // Cyan
        private static final String INFO_COLOR = "\u001B[32m"; // Verde
        private static final String WARN_COLOR = "\u001B[33m"; // Amarillo
        private static final String ERROR_COLOR = "\u001B[31m"; // Rojo

        public String getColor() {
            switch (this) {
                case DEBUG:
                    return DEBUG_COLOR;
                case INFO:
                    return INFO_COLOR;
                case WARN:
                    return WARN_COLOR;
                case ERROR:
                    return ERROR_COLOR;
                default:
                    return RESET_COLOR;
            }
        }
    }

    // Control de si los colores deben ser habilitados
    private static boolean enableColors = true;

    // Formato para la fecha y hora
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // Configura si los colores están habilitados o no
    public static void setEnableColors(boolean enable) {
        enableColors = enable;
    }

    // Método para registrar información con color
    public static void logInfo(String message) {

        StackTraceElement element = Thread.currentThread().getStackTrace()[2];
        String className = element.getClassName();
        String methodName = element.getMethodName();
        int lineNumber = element.getLineNumber();

        // Formatear la fecha y hora actual
        String dateTime = dateFormatter.format(new Date());

        // Usar StringBuilder para mejorar la eficiencia al concatenar cadenas
        StringBuilder logMessage = new StringBuilder();

        // Formato del log
        logMessage.append(String.format("[%s] ", dateTime));

        // Si los colores están habilitados, mostrar con el color adecuado para el nivel
        /*if (enableColors) {
            logMessage.append(level.getColor()).append("[").append(level).append("]").append(RESET_COLOR).append(" ");
        } else {
            logMessage.append("[").append(level).append("] ").append(" ");
        }*/

        // Agregar la información sobre la clase, el método y la línea
        logMessage.append(CLASS_COLOR).append(className).append(RESET_COLOR)
                .append("::").append(METHOD_COLOR).append(methodName).append(RESET_COLOR)
                .append(" (Línea: ").append(LINE_NUMBER_COLOR).append(lineNumber).append(RESET_COLOR).append(") - ");

        // Agregar el mensaje del log
        logMessage.append(MESSAGE_COLOR).append(message).append(RESET_COLOR);

        // Imprimir el mensaje de log en consola
        System.out.println(logMessage.toString());


        // Obtener la información de la pila de ejecución (StackTrace)


        // Usar String.format() para mejorar la legibilidad del log
        /*String logMessage = String.format("Clase: %s, Método: %s, Línea: %d, Log: %s",
                className, methodName, lineNumber, message);*/

        // Códigos ANSI para colores diferentes para cada parte
        /*String classColor = "\u001B[34m"; // Azul
        String methodColor = "\u001B[32m"; // Verde
        String lineNumberColor = "\u001B[33m"; // Amarillo
        String messageColor = "\u001B[35m"; // Magenta
        String resetColor = "\u001B[0m"; // Restablecer el color

        // Imprimir cada parte del log con un color diferente
        System.out.println(classColor + "Clase: " + className + resetColor + ", " +
                methodColor + "Método: " + methodName + resetColor + ", " +
                lineNumberColor + "Línea: " + lineNumber + resetColor + ", " +
                messageColor + "Log: " + message + resetColor);*/
    }

    // Método para registrar información con colores y formato claro
    public static void log(LogLevel level, String message) {
        // Obtener la información de la pila de ejecución (StackTrace) solo una vez
        StackTraceElement element = Thread.currentThread().getStackTrace()[2];
        String className = element.getClassName();
        String methodName = element.getMethodName();
        int lineNumber = element.getLineNumber();

        // Formatear la fecha y hora actual
        String dateTime = dateFormatter.format(new Date());

        // Usar StringBuilder para mejorar la eficiencia al concatenar cadenas
        StringBuilder logMessage = new StringBuilder();

        // Formato del log
        logMessage.append(String.format("[%s] ", dateTime));

        // Si los colores están habilitados, mostrar con el color adecuado para el nivel
        if (enableColors) {
            logMessage.append(level.getColor()).append("[").append(level).append("]").append(RESET_COLOR).append(" ");
        } else {
            logMessage.append("[").append(level).append("] ").append(" ");
        }

        // Agregar la información sobre la clase, el método y la línea
        logMessage.append(CLASS_COLOR).append(className).append(RESET_COLOR)
                .append("::").append(METHOD_COLOR).append(methodName).append(RESET_COLOR)
                .append(" (Línea: ").append(LINE_NUMBER_COLOR).append(lineNumber).append(RESET_COLOR).append(") - ");

        // Agregar el mensaje del log
        logMessage.append(MESSAGE_COLOR).append(message).append(RESET_COLOR);

        // Imprimir el mensaje de log en consola
        System.out.println(logMessage.toString());
    }

    // Métodos específicos para cada nivel de log
    /*public static void logInfo(String message) {
        log(LogLevel.INFO, message);
    }*/

    public static void logWarn(String message) {
        log(LogLevel.WARN, message);
    }

    public static void logError(String message) {
        log(LogLevel.ERROR, message);
    }

    public static void logDebug(String message) {
        log(LogLevel.DEBUG, message);
    }


}
