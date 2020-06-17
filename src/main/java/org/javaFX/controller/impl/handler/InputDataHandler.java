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

public class InputDataHandler extends DataHandler {

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

    public InputDataHandler () {}

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
        handleKeyPressed(confirmNumberButton);
    }

    @FXML
    private void handlePhoneNubmerAreaPressed(){
        handleKeyPressed(confirmNumberButton);
    }

    @FXML
    private void handleConfirmVCPressed(){
       handleKeyPressed(confirmVCButton);
    }

    @FXML
    private void handleVerificationCodeAreaPressed(){
        handleKeyPressed(confirmNumberButton);
    }

    @FXML
    private void handleSignInPressed(){
        handleKeyPressed(signInButton);
    }

    @FXML
    private void handlePasswordAreaPressed(){
        handleKeyPressed(confirmNumberButton);
    }


    private void handleKeyPressed(Node node){
        AtomicBoolean keysPressed = KeyboardHandler.handleEnterPressed(node);
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if ( keysPressed.get() ) {
                    handleConfirmNumberAction();
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
