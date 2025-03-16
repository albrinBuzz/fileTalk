#include <iostream>
#include "src/models/socket/socket_factory.h"
#include "src/models/server/servidor.h"
#include "src/models/server/clienthandler.h"

#include "src/models/socket/unixsocket.h"

int main() {

    Servidor server;

    server.startServer();


    return 0;
}
