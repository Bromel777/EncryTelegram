package org.javaFX.controller.impl.handler;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.encryfoundation.tg.javaIntegration.BackMsg;
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
            BackMsg msg = new BackMsg.CreatePrivateGroupChat(secretChatName.getText());
            getUserStateRef().get().outQueue.put(msg);
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
        BackMsg msg = new BackMsg.DeleteCommunity(secretChatName.getText());
        try {
            getUserStateRef().get().outQueue.put(msg);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        getDialogStage().close();
        getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToCommunitiesListWindowFXML);
    }

    public void setSecretChatNameText(String secretChatNameStr) {
        secretChatName.setText(secretChatNameStr);
    }
}
