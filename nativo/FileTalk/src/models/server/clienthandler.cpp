// <one line to give the program's name and a brief idea of what it does.>
// SPDX-FileCopyrightText: 2025 <copyright holder> <email>
// SPDX-License-Identifier: GPL-3.0-or-later

#include "clienthandler.h"


ClientHandler::ClientHandler(Socket* socket, Servidor* serverP)
    :clienteSocket(socket),server(serverP),ip(socket->getRemoteAddress())
{}

ClientHandler::ClientHandler(Socket* socket)
    :clienteSocket(socket),ip(socket->getRemoteAddress()){}


std::string ClientHandler::getNick() const {
    return nick;
}


void ClientHandler::operator()()
{

    try{


        std::cout << "Manejando al cliente con dirección: "
        << clienteSocket->getRemoteAddress() << std::endl;

          try{

            while(true){


                std::vector<uint8_t> mensajeBt = clienteSocket->receiveBytes(1024);

                std::string mesajeStr(mensajeBt.begin(), mensajeBt.end());

                    std::cout << "Mensaje  del cliente: " << mesajeStr << std::endl;

            }


        }catch(const std::exception& e){
                std::cerr << "Error manejando cliente: " << e.what() << std::endl;
        }



    } catch (const std::exception& e) {
        std::cerr << "Error: " << e.what() << std::endl;
        shutDown();
    }

}

std::string bytesToString(const std::vector<uint8_t>& bytes) {
    return std::string(reinterpret_cast<const char*>(bytes.data()), bytes.size());
}

ClientHandler::ClientHandler()
{

}

ClientHandler::~ClientHandler()
{

}

void ClientHandler::shutDown() {
    //server->removeClient(clienteSocket);
    clienteSocket->close();
    std::cout << "Client " << nick << " disconnected" << std::endl;
}


