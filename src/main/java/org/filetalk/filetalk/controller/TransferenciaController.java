package org.filetalk.filetalk.controller;

import org.filetalk.filetalk.Client.TransferManager;
import org.filetalk.filetalk.model.Observers.TransferencesObserver;
import org.filetalk.filetalk.shared.FileTransferState;

import org.filetalk.filetalk.shared.Logger;
import org.filetalk.filetalk.models.Transferencia;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TransferenciaController {

    private Map<String, Transferencia> transferMap; // Mapa de transferencias activas
    //private TransferencesView view; // Vista para notificar cambios
    private TransferencesObserver transferencesObserver;

    // Constructor
    /*public TransferenciaController(TransferencesView view) {
        this.view = view;
        this.transferMap = new HashMap<>();
    }*/

    public TransferenciaController() {
        this.transferMap = new HashMap<>();
    }

    // Método para agregar una nueva transferencia
    public String  addTransference(String mode, String srcAddr, String dstAddr, String fileName, TransferManager transferManager) {
        UUID uuid = UUID.randomUUID();

        String uniqueIdString = uuid.toString();

        Transferencia transferencia = new Transferencia(uniqueIdString,fileName, srcAddr, dstAddr, FileTransferState.IN_PROGRESS, transferManager);

        transferMap.put(uniqueIdString, transferencia);
        if (transferencesObserver!=null){
            transferencesObserver.addTransference(mode, transferencia,transferManager);
        }

        ///transferencesObserver.addTransference("send", recipientNick, recipientNick,filePath.substring(filePath.lastIndexOf(File.separator)),this);
        Logger.logInfo("Transferencia agregada: " + fileName);
        // Notificar a la vista para que actualice la interfaz
        //Platform.runLater(() -> view.addTransferenceControlPanel(mode, fileName, transferManager));

        return uniqueIdString;
    }

    // Método para actualizar el progreso de una transferencia
    public void updateProgress(FileTransferState transferState,String id, int progress) {
        //transferencesObserver.updateTransference(FileTransferState.RECEIVING, recipientNick, (int)((totalBytesRead * 100) / fileSize));
        Transferencia transferencia = transferMap.get(id);
        if (transferencia != null) {
            transferencia.setProgress(progress);
            //Logger.logInfo("Actualizando progreso de la transferencia: " + id + " - " + progress + "%");
            //transferencesObserver.updateTransference(FileTransferState.SENDING, id, (int) ((totalBytesReaded * 100) / length));
            if (transferencesObserver!=null){
                transferencesObserver.updateTransference(transferState, id,progress);
            }

            // Notificar a la vista para actualizar la barra de progreso
            //Platform.runLater(() -> view.updateTransferenceProgress(fileName, progress));
        }else {
            Logger.logInfo("no existe la transferencia");
        }
    }

    // Método para cambiar el estado de una transferencia (pausar, reanudar, cancelar)
    public void changeState(String fileName, FileTransferState newState) {
        Transferencia transferencia = transferMap.get(fileName);
        if (transferencia != null) {
            transferencia.setState(newState);
            Logger.logInfo("Cambiando estado de la transferencia: " + fileName + " a " + newState);

            // Notificar a la vista sobre el cambio de estado
            //Platform.runLater(() -> view.updateTransferenceState(fileName, newState));
        }
    }

    // Pausar una transferencia
    public void pauseTransference(String fileName) {
        Transferencia transferencia = transferMap.get(fileName);
        if (transferencia != null && transferencia.getState() == FileTransferState.IN_PROGRESS) {
            transferencia.pause();
            changeState(fileName, FileTransferState.PAUSED);
        }
    }

    // Reanudar una transferencia
    public void resumeTransference(String fileName) {
        Transferencia transferencia = transferMap.get(fileName);
        if (transferencia != null && transferencia.getState() == FileTransferState.PAUSED) {
            transferencia.resume();
            changeState(fileName, FileTransferState.IN_PROGRESS);
        }
    }

    // Cancelar una transferencia
    public void cancelTransference(String fileName) {
        Transferencia transferencia = transferMap.get(fileName);
        if (transferencia != null) {
            transferencia.cancel();
            changeState(fileName, FileTransferState.CANCELLED);
        }
    }

    // Eliminar una transferencia una vez completada o cancelada
    public void removeTransference(String fileName) {
        Transferencia transferencia = transferMap.remove(fileName);
        if (transferencia != null) {
            Logger.logInfo("Transferencia eliminada: " + fileName);
            // Notificar a la vista para que actualice la interfaz
            //Platform.runLater(() -> view.removeTransferenceControlPanel(fileName));
        }
    }

    // Finalizar una transferencia (llamado cuando se completa o se ha cancelado)
    public void endTransference(String fileName) {
        removeTransference(fileName);
    }

    // Obtener una transferencia por su nombre
    public Transferencia getTransference(String fileName) {
        return transferMap.get(fileName);
    }

    // Método para obtener todas las transferencias
    public Map<String, Transferencia> getAllTransferencias() {
        return transferMap;
    }

    public void setTransferencesObserver(TransferencesObserver transferencesObserver) {
        this.transferencesObserver = transferencesObserver;
    }
}
