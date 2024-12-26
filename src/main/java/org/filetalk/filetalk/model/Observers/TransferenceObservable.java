package org.filetalk.filetalk.model.Observers;

public interface TransferenceObservable<TransferenceObserver> {
    void addTransferenceObserver(TransferenceObserver observer);

}