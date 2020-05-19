package org.javaFX.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.javaFX.EncryWindow;

public class InputDataHandler {

    @FXML
    private Stage stage;
    @FXML
    private TextField phoneNumber;
    @FXML
    private ImageView resetPhoneNumberImage;
    @FXML
    private TextField verificationCode;
    @FXML
    private TextField password;
    @FXML
    private Button confirmVCButton;
    @FXML
    private ImageView resetVCImage;
    @FXML
    private ImageView resetPasswordImage;
    @FXML
    private Button signInButton;

    private final String imageCrossPath = "file:src/main/resources/images/simpleCross.png";


    public InputDataHandler() {
        initImageViews();
    }

    private void initImageViews(){
        Image crossImage = new Image(getClass().getResourceAsStream(imageCrossPath));
        resetPhoneNumberImage = new ImageView(crossImage);
        resetVCImage = new ImageView(crossImage);
        resetPasswordImage = new ImageView(crossImage);
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

    @FXML
    public void resetPhoneNumberInputAction(){
        System.out.println("hello from reset number");
        phoneNumber.setText("");
        resetVCImage.setVisible(true);
        resetVCImage.setDisable(false);
    }

    @FXML
    public void resetVCAction(){
        System.out.println("hello from reset vc");
        verificationCode.setText("");
        resetPasswordImage.setVisible(true);
        resetPasswordImage.setDisable(false);
    }

    @FXML
    public void resetPasswordAction(){
        System.out.println("hello from reset password");
        password.setText("");
    }

}
