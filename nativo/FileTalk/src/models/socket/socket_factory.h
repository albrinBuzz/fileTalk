#pragma once
#include "socket_factory.h"

#ifdef __unix__  // En Unix/Linux/macOS

#include "unixsocket.h"

#elif defined(_WIN32)  // En Windows
#include <winsock2.h>
#include <ws2tcpip.h>

#else
#error "Plataforma no soportada"
#endif


class SocketFactory{

public:
    static Socket* createSocket(){
      return new UnixSocket();
    }

};
