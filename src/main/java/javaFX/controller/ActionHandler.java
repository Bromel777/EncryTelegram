package javaFX.controller;

import javaFX.EncryWindow;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.stage.Stage;

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
