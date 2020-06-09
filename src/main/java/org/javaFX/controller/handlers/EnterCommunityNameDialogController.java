package org.javaFX.controller.handlers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.javaFX.EncryWindow;
import org.javaFX.model.JLocalCommunity;
import org.javaFX.model.JUserState;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public class EnterCommunityNameDialogController{

    private Stage dialogStage;
    private JLocalCommunity localCommunity;
    private EncryWindow encryWindow;
    private AtomicReference<JUserState> state;

    @FXML
    private TextField nameTextField;

    @FXML
    private void initialize() {
    }

    public EnterCommunityNameDialogController( ) {
    }

    public void setState(AtomicReference<JUserState> state) {
        this.state = state;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setLocalCommunity(JLocalCommunity localCommunity) {
        this.localCommunity = localCommunity;
    }

    public void setEncryWindow(EncryWindow encryWindow) {
        this.encryWindow = encryWindow;
    }

    @FXML
    private void cancelButtonAction(){
        dialogStage.close();
    }

    @FXML
    private void createButtonAction(){
        localCommunity.setCommunityName(nameTextField.getText());
        encryWindow.launchWindowByPathToFXML(EncryWindow.pathToMainWindowFXML);
        /*
        state.set....()
         */
        dialogStage.close();
    }

}