package org.filetalk.filetalk.model.Observers;

import org.filetalk.filetalk.Client.FileTransferManager;
import org.filetalk.filetalk.shared.FileTransferState;

public interface TransferencesObserver{
    void addTransference(String mode, String src_addr, String dst_addr, String fileName, FileTransferManager  transferManager);
    void updateTransference(FileTransferState mode, String addr, int progress);
    void endTransference(String mode, String addr);

    void notifyException(String message);
}
