#include <iostream>
#include "src/models/socket/socket_factory.h"
#include "src/models/server/servidor.h"


int main(int argc, char** argv) {


    std::cout << "Creando el servidor..." << std::endl;


    Servidor servidor;


    servidor.startServer();



    return 0;
}
