package org.javaFX.controller.impl.dialog;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.encryfoundation.tg.javaIntegration.JavaInterMsg;
import org.javaFX.EncryWindow;
import org.javaFX.controller.DialoguController;

import java.util.List;
import java.util.stream.Collectors;

public class EnterCommunityNameDialogController extends DialoguController {

    @FXML
    private TextField nameTextField;

    @FXML
    private void initialize() {
    }

    public EnterCommunityNameDialogController( ) {
    }

    @FXML
    private void createButtonAction() throws InterruptedException {
        getLocalCommunity().setCommunityName(nameTextField.getText());
        getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToMainWindowFXML);
        List<String> members = getLocalCommunity().getCommunityMembers()
                .stream()
                .map(elem -> elem.getPhoneNumber().getValue())
                .collect(Collectors.toList());
        JavaInterMsg msg = new JavaInterMsg.CreateCommunityJava(
                getLocalCommunity().getCommunityName(),
                members
        );
        getState().get().msgsQueue.put(msg);
        getDialogStage().close();
    }



}