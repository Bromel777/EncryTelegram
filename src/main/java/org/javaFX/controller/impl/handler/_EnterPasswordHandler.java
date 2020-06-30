package org.javaFX.controller.impl.handler;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.image.ImageView;
import org.encryfoundation.tg.javaIntegration.AuthMsg;
import org.encryfoundation.tg.javaIntegration.JavaInterMsg;
import org.javaFX.EncryWindow;
import org.javaFX.controller.DataHandler;
import org.javaFX.util.KeyboardHandler;

import java.util.concurrent.atomic.AtomicBoolean;

public class _EnterPasswordHandler extends DataHandler {

    @FXML
    private PasswordField passwordField;

    @FXML
    private ImageView nextButtonImg;

    @FXML
    private Label error;

    public _EnterPasswordHandler() {
    }

    @FXML
    private void handleConfirmPasswordAction(){
        String passwordStr = passwordField.getCharacters().toString();
        try {
            getUserStateRef().get().msgsQueue.put(new JavaInterMsg.SetPass(passwordStr));
            AuthMsg nextStep = getUserStateRef().get().authQueue.take();
            if (nextStep.code() == AuthMsg.loadChats().code())
                getEncryWindow().launchWindowByPathToFXML(
                        EncryWindow.pathToChatsWindowFXML, EncryWindow.afterInitializationWidth,  EncryWindow.afterInitializationHeight
                );
            else if (nextStep.code() == AuthMsg.err().code()) {
                error.setText("Incorrect password");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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