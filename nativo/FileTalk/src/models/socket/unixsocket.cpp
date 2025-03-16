// UnixSocket.cpp
#include "unixsocket.h"
#include <iostream>
#include <cstring>  // Para memset
#include <stdexcept> // Para excepciones
#include <unistd.h>
#include <arpa/inet.h>
#include "unixsocket.h"
#include <fcntl.h>  // Para fcntl()
#include <sys/ioctl.h>  // Para FIONBIO

UnixSocket::UnixSocket() {
    sock = socket(AF_INET, SOCK_STREAM, 0);
    if (sock < 0) {
        throw std::runtime_error("Error al crear el socket");
    }
}

UnixSocket::UnixSocket(int fd) : sock(fd) {}

UnixSocket::~UnixSocket() {
    if (sock >= 0) {
        ::close(sock);
    }
}


void UnixSocket::connect(const std::string& host, int port) {
    std::cout << "Intentando conectar..." << std::endl;

    sockaddr_in server_addr;
    memset(&server_addr, 0, sizeof(server_addr));  // Inicializar a cero
    server_addr.sin_family = AF_INET;
    server_addr.sin_port = htons(port);
    server_addr.sin_addr.s_addr = inet_addr(host.c_str());

    std::cout << "Llamando a connect()" << std::endl;

    if (::connect(sock, (struct sockaddr*)&server_addr, sizeof(server_addr)) < 0) {
        std::cout << "Conexión fallida, errno: " << errno << std::endl;
        throw std::runtime_error("Error al conectar al servidor");
    }

    std::cout << "Conexión exitosa" << std::endl;
}



void UnixSocket::bind(int port) {
    sockaddr_in addr;
    memset(&addr, 0, sizeof(addr));  // Inicializar a cero
    addr.sin_family = AF_INET;
    addr.sin_port = htons(port);
    addr.sin_addr.s_addr = INADDR_ANY;

    if (::bind(sock, (struct sockaddr*)&addr, sizeof(addr)) < 0) {
        throw std::runtime_error("Error al vincular el socket");
    }
}

void UnixSocket::listen(int backlog) {
    if (::listen(sock, backlog) < 0) {
        throw std::runtime_error("Error al escuchar en el socket");
    }
}

Socket* UnixSocket::accept() {
    int client_sock = ::accept(sock, nullptr, nullptr);
    if (client_sock < 0) {
        throw std::runtime_error("Error al aceptar la conexión");
    }
    return new UnixSocket(client_sock);
}

void UnixSocket::send(const std::string& data) {
    ssize_t sent_bytes = ::send(sock, data.c_str(), data.size(), 0);
    if (sent_bytes < 0) {
        throw std::runtime_error("Error al enviar los datos");
    }
}

void UnixSocket::sendBytes(const std::vector<uint8_t>& data) {
    ssize_t sent_bytes = ::send(sock, reinterpret_cast<const char*>(data.data()), data.size(), 0);
    if (sent_bytes < 0) {
        throw std::runtime_error("Error al enviar los bytes");
    }
}

std::vector<uint8_t> UnixSocket::receiveBytes(size_t size) {
    std::vector<uint8_t> buffer(size);
    ssize_t length = ::recv(sock, reinterpret_cast<char*>(buffer.data()), size, 0);
    if (length < 0) {
        throw std::runtime_error("Error al recibir los datos");
    }
    buffer.resize(length);  // Ajustar el tamaño al número real de bytes leídos
    return buffer;
}

void UnixSocket::close() {
    if (sock >= 0) {
        ::close(sock);
        sock = -1;
    }
}

std::string UnixSocket::getLocalAddress() const {
    struct sockaddr_in local_address;
    socklen_t addr_len = sizeof(local_address);

    if (getsockname(sock, (struct sockaddr*)&local_address, &addr_len) == -1) {
        throw std::runtime_error("Error al obtener la dirección local");
    }

    char ip_str[INET_ADDRSTRLEN];
    inet_ntop(AF_INET, &local_address.sin_addr, ip_str, sizeof(ip_str));

    return ip_str;
}

std::string UnixSocket::getRemoteAddress() const {
    struct sockaddr_in remote_address;
    socklen_t addr_len = sizeof(remote_address);

    if (getpeername(sock, (struct sockaddr*)&remote_address, &addr_len) == -1) {
        throw std::runtime_error("Error al obtener la dirección remota");
    }

    char ip_str[INET_ADDRSTRLEN];
    inet_ntop(AF_INET, &remote_address.sin_addr, ip_str, sizeof(ip_str));

    return ip_str;
}



bool UnixSocket::isConnected() {

    setNonBlocking(sock);
    char buff;
    ssize_t result = ::recv(sock, &buff, 1, MSG_PEEK);

    if (result == 0) {
        return false;  // El socket fue cerrado por la otra parte
    } else if (result == -1) {
        if (errno == EINTR || errno == EAGAIN) {
            return true;  // Error temporal o interrumpido, pero el socket sigue activo
        }
        return false;  // Error grave o desconexión
    }

    return true;  // El socket sigue conectado
}

  void UnixSocket::setNonBlocking(int socket_fd)
{

int flag = fcntl(socket_fd,0);

if (flag == -1) {
    std::cerr << "Error al obtener las flags del socket" << std::endl;
    return;
}

fcntl(socket_fd,F_SETFL,flag |  O_NONBLOCK);

}
