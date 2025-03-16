// <one line to give the program's name and a brief idea of what it does.>
// SPDX-FileCopyrightText: 2025 <copyright holder> <email>
// SPDX-License-Identifier: GPL-3.0-or-later

#ifndef SERVIDOR_H
#define SERVIDOR_H
#include "../socket/socket_factory.h"


/**
 * @todo write docs
 */
class Servidor
{

private:
    Socket* serverSocket;
    int PORT;




public:
    /**
     * Default constructor
     */
    Servidor();

    Servidor(int PORT_P);

    void startServer();

    /**
     * Destructor
     */
    ~Servidor();

};

#endif // SERVIDOR_H
