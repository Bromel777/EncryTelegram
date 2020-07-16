package org.javaFX.controller;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import org.javaFX.EncryWindow;
import org.javaFX.model.JLocalCommunity;
import org.javaFX.model.JUserState;

import java.util.concurrent.atomic.AtomicReference;

public abstract class DialogController {

    private Stage dialogStage;
    private JLocalCommunity localCommunity;
    private EncryWindow encryWindow;
    private AtomicReference<JUserState> state;
    private String parentPageNameStr;

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

    public AtomicReference<JUserState> getUserStateRef() {
        return state;
    }

    public void setUserStateRef(AtomicReference<JUserState> state) {
        this.state = state;
    }

    public void setParentPageName(String parentPage) {
        this.parentPageNameStr = parentPage;
    }

    public String getParentPageName() {
        return parentPageNameStr;
    }

    @FXML
    public void closeDialog(){
        getEncryWindow().launchWindowByPathToFXML(parentPageNameStr);
        getDialogStage().close();
    }

}
