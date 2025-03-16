// socket_abstraction.h
#ifndef SOCKET_ABSTRACTION_H
#define SOCKET_ABSTRACTION_H

#include <string>
#include <vector>
#include <cstdint>

class Socket {
public:
    virtual ~Socket() {}
    virtual void connect(const std::string& host, int port) = 0;
    virtual void bind(int port) = 0;
    virtual void listen(int backlog = 5) = 0;
    virtual Socket* accept() = 0;
    virtual void send(const std::string& data) = 0;
    virtual void sendBytes(const std::vector<uint8_t>& data) = 0;
    virtual std::vector<uint8_t> receiveBytes(size_t size) = 0;
    virtual void close() = 0;
    virtual std::string getLocalAddress() const = 0;
    virtual std::string getRemoteAddress() const = 0;
    virtual bool isConnected() = 0;
};

#endif // SOCKET_ABSTRACTION_H
