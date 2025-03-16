
#include <iostream>
#include "src/models/socket/socket_factory.h"

#define PORT 8080  // Puerto en el que el servidor escuchará

#define BACKLOG 5  // Número máximo de conexiones en espera

int main(int argc, char** argv) {


    try {
        // Crear el socket usando el factory
        std::cout << "Creacion del socket Servidor" << std::endl;

        Socket* socket = SocketFactory::createSocket();


        socket->bind(PORT);
        socket->listen(5);

        std::cout << "Servidor escuchando en el puerto " << PORT << "..." << std::endl;

        Socket* socketCliente=socket->accept();
        std::cout << "Conexión aceptada socket direccion:"+socketCliente->getRemoteAddress() << std::endl;
        std::cout << "Direcciones:\n" << std::endl;
        std::cout << socketCliente->getRemoteAddress()+"\n" << std::endl;
        std::cout << socketCliente->getLocalAddress() << std::endl;



        //std::cout << "Conexión aceptada desde " << inet_ntoa(client_address.sin_addr) << ":" << ntohs(client_address.sin_port) << std::endl;

        socket->close();
        delete socket;
    } catch (const std::exception& e) {
        std::cerr << "Error: " << e.what() << std::endl;
    }

    return 0;
}
