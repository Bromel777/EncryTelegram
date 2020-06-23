package org.javaFX.controller.impl.dialog;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.encryfoundation.tg.javaIntegration.JavaInterMsg;
import org.javaFX.EncryWindow;
import org.javaFX.controller.DialogController;

public class CreatePrivateCommonDialogController extends DialogController {

    @FXML
    private TextField privateChatTitle;

    @FXML
    private void createButtonAction() throws InterruptedException {
        JavaInterMsg msg = new JavaInterMsg.CreatePrivateGroupChat(getLocalCommunity().getCommunityName());
        //TODO
        // здесь создаётся приватный чат. В классе надо создавать метод для инъекции названия, например:
        // msg.setTitle(privateChatTitle.getText());

        getState().get().msgsQueue.put(msg);
        getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToMainWindowFXML);
        getDialogStage().close();
    }

}