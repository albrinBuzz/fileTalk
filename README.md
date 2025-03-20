# Proyecto de Transferencia de Archivos en Red Local

**Descripción**: Este proyecto proporciona una solución para la transferencia rápida y segura de archivos entre dispositivos en una red local. Sirve como un reemplazo de un dispositivo USB, permitiendo a los usuarios compartir archivos fácilmente sin necesidad de dispositivos físicos, utilizando solo su red local.

## Requisitos de Ejecución

Antes de ejecutar el proyecto, asegúrate de tener los siguientes requisitos:

- **Java Runtime Environment (JRE) o Java Development Kit (JDK)** versión **17** o superior.
- **JavaFX**: Para la interfaz gráfica de usuario (GUI), se requiere la biblioteca JavaFX.

  Si usas una versión de JDK que no incluye JavaFX por defecto (por ejemplo, JDK 11 o superior), necesitarás descargar e incluir JavaFX en tu proyecto. Puedes obtener la versión más reciente de JavaFX [aquí](https://openjfx.io/).

## ¿Cómo Funciona?

Este proyecto actúa como un reemplazo de un dispositivo USB, utilizando la red local para transferir archivos entre dispositivos. Los archivos se envían a través de la red de forma rápida y segura, sin necesidad de dispositivos físicos, lo que hace que el proceso de transferencia sea más eficiente y flexible.

1. **Conectar a la red local**: Los dispositivos deben estar conectados a la misma red local para poder transferir archivos.
2. **Iniciar el servidor**: El servidor debe ser iniciado en uno de los dispositivos de la red local, que actuará como el punto de recepción de los archivos.
3. **Conectar al servidor**: Los demás dispositivos deben conectarse al servidor utilizando la aplicación, para poder transferir los archivos.
4. **Iniciar la aplicación**: Los usuarios pueden iniciar la aplicación en sus dispositivos para comenzar a enviar y recibir archivos.
5. **Transferencia**: Los archivos seleccionados se envían a través de la red, utilizando encriptación para proteger los datos durante el proceso.
6. **Monitoreo y control**: El estado de las transferencias se puede monitorear en tiempo real y pausar o reanudar según sea necesario.

## Instalación

### 1. Clonar el repositorio

```bash
git clone https://github.com/tuusuario/nombre-del-proyecto.git
