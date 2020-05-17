package org.javaFX.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.javaFX.EncryWindow;

public class InputDataHandler {

    @FXML
    private Stage stage;
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

    public InputDataHandler() {
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
        System.out.println(pass);
    }

}
