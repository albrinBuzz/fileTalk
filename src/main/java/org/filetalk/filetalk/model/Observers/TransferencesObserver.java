package org.filetalk.filetalk.model.Observers;

import org.filetalk.filetalk.Client.FileTransferManager;

import java.util.List;

public interface TransferencesObserver{
    void addTransference(String mode, String src_addr, String dst_addr, String fileName, FileTransferManager  transferManager);
    void updateTransference(String mode, String addr, int progress);
    void endTransference(String mode, String addr);

    void notifyException(String message);
}
