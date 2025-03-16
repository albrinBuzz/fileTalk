// <one line to give the program's name and a brief idea of what it does.>
// SPDX-FileCopyrightText: 2025 <copyright holder> <email>
// SPDX-License-Identifier: GPL-3.0-or-later

#include "servidor.h"
#include "../socket/socket_factory.h"
#include "clienthandler.h"
#include <iostream>
#include <thread>
Servidor::Servidor(int PORT_P): serverSocket(nullptr),PORT(PORT_P)
{

    serverSocket=SocketFactory::createSocket();
    serverSocket->bind(PORT);
    serverSocket->listen(5);


}

Servidor::Servidor():serverSocket(nullptr),PORT(8080)
{
    serverSocket=SocketFactory::createSocket();
    serverSocket->bind(PORT);
    serverSocket->listen(5);
}

Servidor::~Servidor()
{

    delete serverSocket;
}


void Servidor::startServer()
{

         std::cout << "Servidor Iniciado\n" << std::endl;

        while(true){

        Socket* clienteSocket=serverSocket->accept();

        ClientHandler* clientHandler=new ClientHandler(clienteSocket);

        std::thread thCliente(*clientHandler);

        // Iniciar el hilo
        thCliente.detach();


        //std::thread(&ClientHandler::run);

        std::cout << "Conexión aceptada socket direccion:"+clienteSocket->getRemoteAddress() << std::endl;
        std::cout << "Direccion:\n" << std::endl;
        std::cout << clienteSocket->getRemoteAddress()+"\n" << std::endl;

    }

}
