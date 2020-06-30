package org.javaFX.controller.impl.handler;

import javafx.animation.AnimationTimer;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import org.encryfoundation.tg.javaIntegration.AuthMsg;
import org.encryfoundation.tg.javaIntegration.JavaInterMsg;
import org.javaFX.EncryWindow;
import org.javaFX.controller.DataHandler;
import org.javaFX.util.KeyboardHandler;

import java.util.concurrent.atomic.AtomicBoolean;

public class _EnterVerificationCodeHandler extends DataHandler {

    @FXML
    private TextField verificationCodeTextField;

    @FXML
    private ImageView nextButtonImg;

    @FXML
    private Label phoneNumberLabel;

    @FXML
    private Label error;

    public _EnterVerificationCodeHandler() {
    }

    @FXML
    private void handleKeyTyped(){
        verificationCodeTextField.addEventFilter(KeyEvent.KEY_TYPED, maxVCLength());
    }

    @FXML
    private void handleConfirmVCAction() {
        String verificationCodeStr = verificationCodeTextField.getCharacters().toString();
        try {
            getUserStateRef().get().msgsQueue.put(new JavaInterMsg.SetVCCode(verificationCodeStr));
            AuthMsg nextStep = getUserStateRef().get().authQueue.take();
            if (nextStep.code() == AuthMsg.loadPass().code()) {
                getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToEnterPasswordWindowFXML);
            } else if (nextStep.code() == AuthMsg.err().code()) {
                error.setText("Incorrect vc code");
            }
            else {
                getEncryWindow().launchWindowByPathToFXML(
                        EncryWindow.pathToChatsWindowFXML, EncryWindow.afterInitializationWidth,  EncryWindow.afterInitializationHeight
                );
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleVerificationCodeAreaPressed(){
        System.out.println("Invoke handleVerificationCodeAreaPressed");
        handleVerificationCodeAccepted(nextButtonImg);
    }

    private void handleVerificationCodeAccepted(Node node){
        System.out.println("Invoke handleVerificationCodeAccepted");
        AtomicBoolean[] keysPressed = KeyboardHandler.handleShiftEnterPressed(node);
        System.out.println("keysPressed " + keysPressed);
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

