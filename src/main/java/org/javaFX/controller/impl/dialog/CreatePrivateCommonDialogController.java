package org.javaFX.controller.impl.dialog;

import javafx.fxml.FXML;
import org.encryfoundation.tg.javaIntegration.JavaInterMsg;
import org.javaFX.EncryWindow;
import org.javaFX.controller.DialoguController;

public class CreatePrivateCommonDialogController extends DialoguController {

    @FXML
    private void createButtonAction() throws InterruptedException {
        JavaInterMsg msg = new JavaInterMsg.CreatePrivateGroupChat(getLocalCommunity().getCommunityName());
        getState().get().msgsQueue.put(msg);
        getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToMainWindowFXML);
        getDialogStage().close();
    }

}
