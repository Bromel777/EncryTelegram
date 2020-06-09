package org.javaFX.controller.handlers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.javaFX.EncryWindow;
import org.javaFX.model.JLocalCommunity;

import java.io.IOException;

public class EnterCommunityNameDialogController{

    private Stage dialogStage;
    private JLocalCommunity localCommunity;
    private EncryWindow encryWindow;

    @FXML
    private TextField nameTextField;

    @FXML
    private void initialize() {
    }

    public EnterCommunityNameDialogController( ) {
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
        dialogStage.close();
    }

}