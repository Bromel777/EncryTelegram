package org.javaFX.controller.impl.handler;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.PasswordField;
import javafx.scene.image.ImageView;
import org.javaFX.EncryWindow;
import org.javaFX.controller.DataHandler;
import org.javaFX.util.KeyboardHandler;

import java.util.concurrent.atomic.AtomicBoolean;

public class _EnterPasswordHandler extends DataHandler {

    @FXML
    private PasswordField passwordField;

    @FXML
    private ImageView nextButtonImg;

    public _EnterPasswordHandler() {
    }

    @FXML
    private void handleConfirmPasswordAction(){
        String passwordStr = passwordField.getCharacters().toString();
        getUserStateRef().get().setPass(passwordStr);
        getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToChatsWindowFXML, 900, 645);
    }

    @FXML
    private void handlePasswordAreaPressed(){
        handlePasswordAccepted(nextButtonImg);
    }

    private void handlePasswordAccepted(Node node){
        AtomicBoolean[] keysPressed = KeyboardHandler.handleShiftEnterPressed(node);
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if ( keysPressed[0].get() && keysPressed[1].get()   ) {
                    handleConfirmPasswordAction();
                }
            }
        }.start();
    }

    @FXML
    private void backToVerificationCodePage(){
        getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToEnterVerificationCodeWindowFXML);
    }

}