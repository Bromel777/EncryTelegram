package org.javaFX.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

public class InputDataHandler extends DataHandler {

    @FXML
    private TextField phoneNumber;
    @FXML
    private ImageView resetPhoneNumberImageView;
    @FXML
    private TextField verificationCode;
    @FXML
    private TextField password;
    @FXML
    private Button confirmVCButton;
    @FXML
    private ImageView resetVCImageView;
    @FXML
    private ImageView resetPasswordImageView;
    @FXML
    private Button signInButton;


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
    }

    @FXML
    public void handleConfirmVCAction(){
        String verificationCodeStr = verificationCode.getCharacters().toString();
        getUserStateRef().get().setCode(verificationCodeStr);
        password.setDisable(false);
        signInButton.setDisable(false);
        verificationCode.setDisable(true);
    }

    @FXML
    public void singInAction(){
        String pass = password.getCharacters().toString();
        getUserStateRef().get().setPass(pass);
        do {} while (! getUserStateRef().get().isAuth());
        getEncryWindow().launchMainWindow();
    }

    @FXML
    public void resetPhoneNumberInputAction(){
        System.out.println("hello from reset number");
        if (getUserStateRef().get().isAuth()) getEncryWindow().launchMainWindow();
        if(!phoneNumber.isDisable()) {
            phoneNumber.setText("");
            resetVCImageView.setVisible(true);
            resetVCImageView.setDisable(false);
            resetPhoneNumberImageView.setDisable(true);
        }
    }

    @FXML
    public void resetVCAction(){
        System.out.println("hello from reset vc");
        if(!verificationCode.isDisable()) {
            verificationCode.setText("");
            resetPasswordImageView.setVisible(true);
            resetPasswordImageView.setDisable(false);
            resetVCImageView.setDisable(true);
        }
    }

    @FXML
    public void resetPasswordAction(){
        System.out.println("hello from reset password");
        if(!password.isDisable()) {
            password.setText("");
        }
    }

}
