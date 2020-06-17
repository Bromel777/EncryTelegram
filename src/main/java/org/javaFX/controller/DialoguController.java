package org.javaFX.controller;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import org.javaFX.EncryWindow;
import org.javaFX.model.JLocalCommunity;
import org.javaFX.model.JUserState;

import java.util.concurrent.atomic.AtomicReference;

public abstract class DialoguController {

    private Stage dialogStage;
    private JLocalCommunity localCommunity;
    private EncryWindow encryWindow;
    private AtomicReference<JUserState> state;

    public Stage getDialogStage() {
        return dialogStage;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public JLocalCommunity getLocalCommunity() {
        return localCommunity;
    }

    public void setLocalCommunity(JLocalCommunity localCommunity) {
        this.localCommunity = localCommunity;
    }

    public EncryWindow getEncryWindow() {
        return encryWindow;
    }

    public void setEncryWindow(EncryWindow encryWindow) {
        this.encryWindow = encryWindow;
    }

    public AtomicReference<JUserState> getState() {
        return state;
    }

    public void setState(AtomicReference<JUserState> state) {
        this.state = state;
    }

    @FXML
    public void cancelButtonAction(){
        getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToMainWindowFXML);
        getDialogStage().close();
    }

}
