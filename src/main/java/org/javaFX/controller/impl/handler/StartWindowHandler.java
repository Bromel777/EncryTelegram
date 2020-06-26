package org.javaFX.controller.impl.handler;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import org.javaFX.controller.DataHandler;
import org.javaFX.util.KeyboardHandler;

import java.util.concurrent.atomic.AtomicBoolean;

public class StartWindowHandler extends DataHandler {

    @FXML
    private Button cancelButton;

    public StartWindowHandler() {
    }

    @FXML
    public void handleCancelAction(){
        getStage().close();
    }

    @FXML
    public void handleKeyPressedAction(){
        AtomicBoolean keysPressed = KeyboardHandler.handleEnterPressed(cancelButton);
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if ( keysPressed.get() ) {
                    handleCancelAction();
                }
            }
        }.start();
    }
}
