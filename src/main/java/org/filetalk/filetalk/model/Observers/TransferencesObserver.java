package org.filetalk.filetalk.model.Observers;

import org.filetalk.filetalk.Client.FileTransferManager;
import org.filetalk.filetalk.Client.TransferManager;
import org.filetalk.filetalk.models.Transferencia;
import org.filetalk.filetalk.shared.FileTransferState;

public interface TransferencesObserver{
    void addTransference(String mode, Transferencia transferencia, TransferManager transferManager);
    void updateTransference(FileTransferState mode, String id, int progress);
    void endTransference(String mode, String addr);

    void notifyException(String message);
}
