package org.javaFX.controller.handlers;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import org.javaFX.EncryWindow;
import org.javaFX.model.JUserState;

import java.util.concurrent.atomic.AtomicReference;

public abstract class DataHandler {
    @FXML
    private Stage stage;
    private EncryWindow encryWindow;
    private AtomicReference<JUserState> userStateRef;

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public EncryWindow getEncryWindow() {
        return encryWindow;
    }

    public void setEncryWindow(EncryWindow encryWindow) {
        this.encryWindow = encryWindow;
    }

    public AtomicReference<JUserState> getUserStateRef() {
        return userStateRef;
    }

    public void setUserStateRef(AtomicReference<JUserState> userStateRef) {
        this.userStateRef = userStateRef;
    }

    public void updateEncryWindow(EncryWindow encryWindow){
        this.setEncryWindow(encryWindow);
    }

}
