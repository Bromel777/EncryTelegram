package org.javaFX.controller.impl.handler;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import org.javaFX.EncryWindow;
import org.javaFX.controller.DataHandler;
import org.javaFX.util.KeyboardHandler;

import java.util.concurrent.atomic.AtomicBoolean;

public class AuthenticationHandler extends DataHandler {

    @FXML
    private TextField phoneNumber;
    @FXML
    private TextField verificationCode;
    @FXML
    private TextField password;
    @FXML
    private Button confirmNumberButton;
    @FXML
    private Button confirmVCButton;
    @FXML
    private Button signInButton;
    @FXML
    private ImageView resetPhoneNumberImageView;
    @FXML
    private ImageView resetVCImageView;
    @FXML
    private ImageView resetPasswordImageView;

    public AuthenticationHandler() {}

    @FXML
    private void handleCancelAction(){
        getStage().close();
    }

    @FXML
    private void handleConfirmNumberAction(){
        String phoneNumberStr = phoneNumber.getCharacters().toString();
        getUserStateRef().get().setPhoneNumber(phoneNumberStr);
        confirmVCButton.setDisable(false);
        verificationCode.setDisable(false);
        confirmNumberButton.setDisable(true);
        phoneNumber.setDisable(true);
        resetVCImageView.setVisible(true);
        resetVCImageView.setDisable(false);
        resetPhoneNumberImageView.setDisable(true);
    }

    @FXML
    private void handleConfirmVCAction(){
        String verificationCodeStr = verificationCode.getCharacters().toString();
        getUserStateRef().get().setCode(verificationCodeStr);
        password.setDisable(false);
        signInButton.setDisable(false);
        confirmVCButton.setDisable(true);
        verificationCode.setDisable(true);
        resetPasswordImageView.setVisible(true);
        resetPasswordImageView.setDisable(false);
        resetVCImageView.setDisable(true);
    }

    @FXML
    private void singInAction(){
        String pass = password.getCharacters().toString();
        getUserStateRef().get().setPass(pass);
        do {} while (! getUserStateRef().get().isAuth());
        getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToMainWindowFXML);
    }

    @FXML
    private void handleConfirmNumberPressed(){
        handleNumberAccepted(confirmNumberButton);
    }

    @FXML
    private void handlePhoneNumberAreaPressed(){
        handleNumberAccepted(confirmNumberButton);
    }

    @FXML
    private void handleConfirmVCPressed(){
        handleVCAccepted(confirmVCButton);
    }

    @FXML
    private void handleVCAreaPressed(){
        handleVCAccepted(confirmVCButton);
    }

    @FXML
    private void handleSignInPressed(){
        handleVPasswordAccepted(signInButton);
    }

    @FXML
    private void handlePasswordAreaPressed(){
        handleVPasswordAccepted(signInButton);
    }


    private void handleNumberAccepted(Node node){
        AtomicBoolean[] keysPressed = KeyboardHandler.handleShiftEnterPressed(node);
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if ( keysPressed[0].get() && keysPressed[1].get()   ) {
                    handleConfirmNumberAction();
                }
            }
        }.start();
    }

    private void handleVCAccepted(Node node){
        AtomicBoolean[] keysPressed = KeyboardHandler.handleShiftEnterPressed(node);
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (keysPressed[0].get() && keysPressed[1].get()   ) {
                    handleConfirmVCAction();
                }
            }
        }.start();
    }

    private void handleVPasswordAccepted(Node node){
        AtomicBoolean[] keysPressed = KeyboardHandler.handleShiftEnterPressed(node);
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if ( keysPressed[0].get() && keysPressed[1].get()  ) {
                    singInAction();
                }
            }
        }.start();
    }

    @FXML
    private void resetPhoneNumberInputAction(){
        if(!phoneNumber.isDisable()) {
            phoneNumber.setText("");
        }
    }

    @FXML
    private void resetVCAction(){
        if(!verificationCode.isDisable()) {
            verificationCode.setText("");
        }
    }

    @FXML
    private void resetPasswordAction(){
        if(!password.isDisable()) {
            password.setText("");
        }
    }

}
