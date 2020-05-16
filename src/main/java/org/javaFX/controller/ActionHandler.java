package org.javaFX.controller;

import org.javaFX.EncryWindow;
import javafx.event.ActionEvent;

public class ActionHandler {
    private EncryWindow encryWindow ;

    public ActionHandler(EncryWindow encryWindow) {
        this.encryWindow = encryWindow;
    }

//    @FXML
    private void cancelButtonAction(ActionEvent event) throws Exception {
        encryWindow.stop();
    }

    public EncryWindow getEncryWindow() {
        return encryWindow;
    }

    public void setMainWindow(EncryWindow encryWindow) {
        this.encryWindow = encryWindow;
    }
}
