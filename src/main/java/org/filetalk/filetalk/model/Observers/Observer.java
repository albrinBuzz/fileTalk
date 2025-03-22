package org.filetalk.filetalk.model.Observers;

import org.filetalk.filetalk.shared.ServerStatusConnection;

import java.util.List;

public interface Observer {
    void updateServerConnection (ServerStatusConnection statusConnection);
    void updateClientsList(List<String> clients);
    void updateMessaje(String message);

}
