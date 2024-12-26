import org.filetalk.filetalk.Client.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.io.*;
import java.net.Socket;

import static org.mockito.Mockito.*;

public class ClientTest {

    private Client client;

    @Mock
    private Socket mockSocket;
    @Mock
    private DataOutputStream mockDataOutputStream;
    @Mock
    private FileInputStream mockFileInputStream;
    @Mock
    private File mockFile;

    @BeforeEach
    void setUp() throws IOException {
        // Inicializar el objeto Client y los mocks
        MockitoAnnotations.openMocks(this);
        client = mock(Client.class); // Mockeamos Client para inyectar los mocks

        // Configurar los mocks de Socket y los flujos
        when(mockSocket.getOutputStream()).thenReturn(mockDataOutputStream);
        when(mockSocket.getInputStream()).thenReturn(mockFileInputStream);

        // Configurar los mocks de File
        when(mockFile.exists()).thenReturn(true);
        when(mockFile.isFile()).thenReturn(true);
        when(mockFile.length()).thenReturn(1024L);
        when(mockFile.getName()).thenReturn("testfile.txt");
    }

    @Test
    void testSendFile() throws IOException {
        // Arrange
        String serverAddress = "localhost";
        int serverPort = 9090;
        String filePath = "/home/cris/Descargas/sqlbrowserfx-fat.jar";
        File file = new File(filePath);

        // Act (Ejecutar la función de enviar el archivo)
        //client.sendFile(file, "/file testUser " + filePath, serverAddress);

        // Assert (Verificar las interacciones)
        verify(mockSocket, times(1)).getOutputStream(); // Asegura que se ha creado el DataOutputStream
        verify(mockDataOutputStream, times(1)).writeUTF("enviando"); // Verifica el primer writeUTF
        verify(mockDataOutputStream, times(1)).writeUTF("/file testUser " + filePath); // Verifica el segundo writeUTF
        verify(mockDataOutputStream, times(1)).writeUTF("testfile.txt"); // Verifica el nombre del archivo
        verify(mockDataOutputStream, times(1)).writeLong(1024L); // Verifica el tamaño del archivo
        verify(mockDataOutputStream, times(1)).write(any(byte[].class), anyInt(), anyInt()); // Verifica que se están enviando los bytes
        verify(mockDataOutputStream, times(1)).flush(); // Verifica que se ha hecho el flush
    }
}
