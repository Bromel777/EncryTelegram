package org.javaFX.controller.impl.handler;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import org.encryfoundation.tg.javaIntegration.JavaInterMsg;
import org.javaFX.EncryWindow;
import org.javaFX.controller.DialogController;

public class SingleCommunityDialogHandler extends DialogController {

    @FXML
    private Label secretChatName;

    @FXML
    private Label errorLabel;

    public SingleCommunityDialogHandler() {
    }

    @FXML
    private void createSecretChat() throws InterruptedException {
        if(!secretChatName.getText().isEmpty()){
            JavaInterMsg msg = new JavaInterMsg.CreatePrivateGroupChat(secretChatName.getText());
            getUserStateRef().get().msgsQueue.put(msg);
            getDialogStage().close();
            getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToChatsWindowFXML);
        }
        else{
            secretChatName.setFocusTraversable(true);
            errorLabel.setVisible(true);
        }
    }

    @FXML
    private void deleteConference() {
        JavaInterMsg msg = new JavaInterMsg.DeleteCommunity(secretChatName.getText());
        try {
            getUserStateRef().get().msgsQueue.put(msg);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        getDialogStage().close();
        getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToChatsWindowFXML);
    }

    public void setSecretChatNameText(String secretChatNameStr) {
        secretChatName.setText(secretChatNameStr);
    }
}
