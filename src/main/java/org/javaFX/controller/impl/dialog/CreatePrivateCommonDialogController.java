package org.javaFX.controller.impl.dialog;

import javafx.fxml.FXML;
import org.javaFX.EncryWindow;
import org.javaFX.controller.DialoguController;

public class CreatePrivateCommonDialogController extends DialoguController {

    @FXML
    private void createButtonAction(){
        //todo
        getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToMainWindowFXML);
        getDialogStage().close();
    }

}
