package org.javaFX.controller.impl.handler;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import org.javaFX.EncryWindow;
import org.javaFX.controller.DataHandler;

public class InputDataHandler extends DataHandler {

    @FXML
    private TextField phoneNumber;
    @FXML
    private TextField verificationCode;
    @FXML
    private TextField password;
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
    public void handleCancelAction(){
        getStage().close();
    }

    @FXML
    public void handleConfirmNumberAction(){
        String phoneNumberStr = phoneNumber.getCharacters().toString();
        getUserStateRef().get().setPhoneNumber(phoneNumberStr);
        confirmVCButton.setDisable(false);
        verificationCode.setDisable(false);
        phoneNumber.setDisable(true);
        resetVCImageView.setVisible(true);
        resetVCImageView.setDisable(false);
        resetPhoneNumberImageView.setDisable(true);
    }

    @FXML
    public void handleConfirmVCAction(){
        String verificationCodeStr = verificationCode.getCharacters().toString();
        getUserStateRef().get().setCode(verificationCodeStr);
        password.setDisable(false);
        signInButton.setDisable(false);
        verificationCode.setDisable(true);
        resetPasswordImageView.setVisible(true);
        resetPasswordImageView.setDisable(false);
        resetVCImageView.setDisable(true);
    }

    @FXML
    public void singInAction(){
        String pass = password.getCharacters().toString();
        getUserStateRef().get().setPass(pass);
        do {} while (! getUserStateRef().get().isAuth());
        getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToMainWindowFXML);
    }

    @FXML
    public void resetPhoneNumberInputAction(){
        if(!phoneNumber.isDisable()) {
            phoneNumber.setText("");
        }
    }

    @FXML
    public void resetVCAction(){
        if(!verificationCode.isDisable()) {
            verificationCode.setText("");
        }
    }

    @FXML
    public void resetPasswordAction(){
        if(!password.isDisable()) {
            password.setText("");
        }
    }

}
