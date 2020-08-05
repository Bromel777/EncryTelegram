package org.javaFX.controller.impl.handler;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.image.ImageView;
import org.encryfoundation.tg.javaIntegration.FrontMsg;
import org.encryfoundation.tg.javaIntegration.BackMsg;
import org.javaFX.EncryWindow;
import org.javaFX.controller.DataHandler;
import org.javaFX.util.KeyboardHandler;

import java.util.concurrent.atomic.AtomicBoolean;

public class EnterPasswordHandler extends DataHandler {

    @FXML
    private PasswordField passwordField;

    @FXML
    private ImageView nextButtonImg;

    @FXML
    private Label error;

    @FXML
    private Label promptLabel;

    public EnterPasswordHandler() {
    }

    @FXML
    private void handleConfirmPasswordAction(){
        String passwordStr = passwordField.getCharacters().toString();
        if (passwordStr.isEmpty()) error.setText("Empty password :( Please enter it!");
        else try {
            getUserStateRef().get().outQueue.put(new BackMsg.SetPass(passwordStr));
            FrontMsg nextStep = getUserStateRef().get().inQueue.take();
            if (nextStep.code() == FrontMsg.Codes$.MODULE$.loadPass()) {
                getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToEnterPasswordWindowFXML);
            } else if (nextStep.code() == FrontMsg.Codes$.MODULE$.loadVc()) {
                getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToEnterVerificationCodeWindowFXML);
                ((EnterVerificationCodeHandler) getEncryWindow().getCurrentController())
                        .setPhoneNumberLabelText(getUserStateRef().get().getPreparedPhoneNumber());
            } else if (nextStep.code() == FrontMsg.Codes$.MODULE$.loadChats()) {
                getEncryWindow().launchWindowByPathToFXML(
                        EncryWindow.pathToChatsWindowFXML, EncryWindow.afterInitializationWidth, EncryWindow.afterInitializationHeight
                );
            } else if (nextStep.code() == FrontMsg.Codes$.MODULE$.error()) {
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