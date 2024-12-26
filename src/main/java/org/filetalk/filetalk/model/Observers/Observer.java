package org.filetalk.filetalk.model.Observers;

import java.util.List;

public interface Observer {
    void update(String message);
    void updateClientsList(List<String> clients);
    void updateMessaje(String message);
}
