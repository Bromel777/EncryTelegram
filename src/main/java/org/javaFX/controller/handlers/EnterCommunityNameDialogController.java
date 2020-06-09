package org.javaFX.controller.handlers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.encryfoundation.tg.javaIntegration.JavaInterMsg;
import org.javaFX.EncryWindow;
import org.javaFX.model.JLocalCommunity;
import org.javaFX.model.JUserState;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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
    private void createButtonAction() throws InterruptedException {
        localCommunity.setCommunityName(nameTextField.getText());
        encryWindow.launchWindowByPathToFXML(EncryWindow.pathToMainWindowFXML);
        List<String> members = localCommunity.getCommunityMembers()
                .stream()
                .map(elem -> elem.getPhoneNumber().toString())
                .collect(Collectors.toList());
        JavaInterMsg msg = new JavaInterMsg.CreateCommunityJava(
                localCommunity.getCommunityName(),
                members
        );
        state.get().msgsQueue.put(msg);
        dialogStage.close();
    }

}