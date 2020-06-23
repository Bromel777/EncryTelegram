package org.javaFX.controller.impl.dialog;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.encryfoundation.tg.javaIntegration.JavaInterMsg;
import org.javaFX.EncryWindow;
import org.javaFX.controller.DialogController;
import org.javaFX.util.InfoContainer;

import java.util.List;
import java.util.stream.Collectors;

public class EnterCommunityNameDialogController extends DialogController {

    @FXML
    private TextField nameTextField;

    private static int localCommunitySize = 0;

    public static int getLocalCommunitySize(){
        return localCommunitySize;
    }

    @FXML
    private void initialize() {
    }

    public EnterCommunityNameDialogController( ) {
    }

    @FXML
    private void createButtonAction() throws InterruptedException {
        getLocalCommunity().setCommunityName(nameTextField.getText());
        getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToMainWindowFXML);
        InfoContainer.addCommunity(getLocalCommunity());
        List<String> members = getLocalCommunity().getCommunityMembers()
                .stream()
                .map(elem -> elem.getPhoneNumber().getValue())
                .collect(Collectors.toList());
        localCommunitySize = members.size();
        JavaInterMsg msg = new JavaInterMsg.CreateCommunityJava(
                getLocalCommunity().getCommunityName(),
                members
        );
        getState().get().msgsQueue.put(msg);
        getDialogStage().close();
    }

}