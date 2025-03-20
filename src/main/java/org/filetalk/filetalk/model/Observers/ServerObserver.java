package org.filetalk.filetalk.model.Observers;

import org.filetalk.filetalk.Client.ClientInfo;

import java.util.List;

public interface ServerObserver {

    void updateClient(List< ClientInfo> clientInfo, int threads);
    void updateUptime(String uptime);

    void updateBytes(String bytes);
}
