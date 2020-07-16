package org.javaFX.controller.impl.handler;

import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.javaFX.controller.DialogController;

public class InfoDialogHandler extends DialogController {

    private MenuItem aboutMenuItem;

    @Override
    public void setDialogStage(Stage dialogStage) {
        super.setDialogStage(dialogStage);
        dialogStage.setOnCloseRequest((WindowEvent we) -> {
            aboutMenuItem.setDisable(false);
        });
    }

    public void setAboutMenuItem(MenuItem aboutMenuItem) {
        this.aboutMenuItem = aboutMenuItem;
    }

    @FXML
    private void closeDialogWindow(){
        closeDialog();
        aboutMenuItem.setDisable(false);
    }



}
