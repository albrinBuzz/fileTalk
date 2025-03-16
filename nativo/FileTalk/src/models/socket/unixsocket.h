// UnixSocket.h
#ifndef UNIX_SOCKET_H
#define UNIX_SOCKET_H

#include "socket_abstraction.h"
#include <string>
#include <vector>

class UnixSocket : public Socket {
private:
    int sock;

public:
    // Constructor
    UnixSocket();

    // Constructor con descriptor de socket
    UnixSocket(int fd);

    // Destructor
    virtual ~UnixSocket();

    // Métodos sobrecargados de la clase Socket
    void connect(const std::string& host, int port) override;
    void bind(int port) override;
    void listen(int backlog = 5) override;
    Socket* accept() override;
    void send(const std::string& data) override;
    void sendBytes(const std::vector<uint8_t>& data) override;
    std::vector<uint8_t> receiveBytes(size_t size) override;
    void close() override;

    std::string getLocalAddress() const override;
    std::string getRemoteAddress() const override;
    bool isConnected() override;

private:

    void setNonBlocking(int socket_fd);

};

#endif // UNIX_SOCKET_H
