// <one line to give the program's name and a brief idea of what it does.>
// SPDX-FileCopyrightText: 2025 <copyright holder> <email>
// SPDX-License-Identifier: GPL-3.0-or-later

#ifndef CLIENTHANDLER_H
#define CLIENTHANDLER_H

#include <iostream>
#include <string>
#include <thread>
#include <mutex>
#include <memory>
#include <regex>
#include <vector>
#include "../socket/socket_abstraction.h"
#include "servidor.h"


/**
 * @todo write docs
 */
class ClientHandler
{

private:
       Socket*  clienteSocket;
       std::string nick;
       std::string ip;
       Servidor* server;

public:
    /**
     * Default constructor
     */
    ClientHandler();

    ClientHandler( Socket*  socket);

    ClientHandler(Socket* socket, Servidor* serverP);

    std::string getNick() const;

    void operator()();

    //void sendCommunication(const Communication& communication);

    //void handleCommunication(const Communication& communication);

    static bool isOnlyLetters(const std::string& text);

    void handleNickChange(const std::string& message);

    void sendFileToClient(const std::string& message);

    void directMessage(const std::string& message);

    void shutDown();


    /**
     * Destructor
     */
    ~ClientHandler();

};

#endif // CLIENTHANDLER_H
