package org.javaFX.controller.impl.handler;

import javafx.animation.AnimationTimer;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import org.javaFX.EncryWindow;
import org.javaFX.controller.DataHandler;
import org.javaFX.util.KeyboardHandler;

import java.util.concurrent.atomic.AtomicBoolean;

public class EnterVerificationCodeHandler extends DataHandler {

    @FXML
    private TextField verificationCodeTextField;

    @FXML
    private ImageView nextButtonImg;

    @FXML
    private Label phoneNumberLabel;

    public EnterVerificationCodeHandler() {
    }

    @FXML
    private void handleKeyTyped(){
        verificationCodeTextField.addEventFilter(KeyEvent.KEY_TYPED, maxVCLength());
    }

    @FXML
    private void handleConfirmVCAction(){
        String verificationCodeStr = verificationCodeTextField.getCharacters().toString();
        getUserStateRef().get().setCode(verificationCodeStr);
        getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToEnterPasswordWindowFXML);
    }

    @FXML
    private void handleVerificationCodeAreaPressed(){
        handleVerificationCodeAccepted(nextButtonImg);
    }

    private void handleVerificationCodeAccepted(Node node){
        AtomicBoolean[] keysPressed = KeyboardHandler.handleShiftEnterPressed(node);
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if ( keysPressed[0].get() && keysPressed[1].get()   ) {
                    handleConfirmVCAction();
                }
            }
        }.start();
    }

    private EventHandler<KeyEvent> maxVCLength() {
        return arg0 -> {
            TextField tx = (TextField) arg0.getSource();
            if (tx.getText().length() >= 5) {
                arg0.consume();
            }
        };
    }

    @FXML
    private void backToEnterPhoneNumberPage(){
        getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToEnterPhoneNumberWindowFXML);
    }

    public void setPhoneNumberLabelText(String text){
        phoneNumberLabel.setText(text);
    }

}

