package org.filetalk.filetalk.model.Observers;

import org.filetalk.filetalk.Client.ClientInfo;

import java.util.List;

public interface HostsObserver {

    void updateAllHosts(List<ClientInfo>hostList);
}
