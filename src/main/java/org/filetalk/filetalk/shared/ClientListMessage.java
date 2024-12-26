package org.filetalk.filetalk.shared;

import org.filetalk.filetalk.Client.ClientInfo;

import java.io.Serializable;
import java.util.List;



public class ClientListMessage extends Communication {

    private List<ClientInfo> clientNicks;  // Lista de nombres de clientes

    public ClientListMessage(CommunicationType communicationType, List<ClientInfo> clientNick) {
        super(communicationType);
        this.clientNicks = clientNick;

    }
    public List<ClientInfo>getClientNicks(){
        return this.clientNicks;
    }

}
