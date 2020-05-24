package org.javaFX.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.javaFX.EncryWindow;

import java.io.IOException;

public class InputDataHandler {

    @FXML
    private Stage stage;
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

    private EncryWindow encryWindow;

    public InputDataHandler () {}

    public void setEncryWindow(EncryWindow encryWindow) {
        this.encryWindow = encryWindow;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void handleCancelAction(){
        stage.close();
        System.out.println("Cancel button was pressed");
    }

    @FXML
    public void handleConfirmNumberAction(){
        System.out.println("hello from confirm tel number");
        String phoneNumberStr = phoneNumber.getCharacters().toString();
        System.out.println(phoneNumberStr);
        confirmVCButton.setDisable(false);
        verificationCode.setDisable(false);
    }

    @FXML
    public void handleConfirmVCAction(){
        System.out.println("hello from confirm VC");
        String verificationCodeStr = verificationCode.getCharacters().toString();
        System.out.println(verificationCodeStr);
        password.setDisable(false);
        signInButton.setDisable(false);
    }

    @FXML
    public void singInAction(){
        System.out.println("hello from sing in");
        String pass = password.getCharacters().toString();
        encryWindow.launchMainWindow();
        System.out.println(pass);
    }


    @FXML
    public void resetPhoneNumberInputAction(){
        System.out.println("hello from reset number");
        phoneNumber.setText("");
        resetVCImageView.setVisible(true);
        resetVCImageView.setDisable(false);
    }

    @FXML
    public void resetVCAction(){
        System.out.println("hello from reset vc");
        verificationCode.setText("");
        resetPasswordImageView.setVisible(true);
        resetPasswordImageView.setDisable(false);
    }

    @FXML
    public void resetPasswordAction(){
        System.out.println("hello from reset password");
        password.setText("");
    }

}
