package org.javaFX.controller.impl.handler;

import javafx.animation.AnimationTimer;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;

import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import org.javaFX.EncryWindow;
import org.javaFX.controller.DataHandler;
import org.javaFX.util.KeyboardHandler;

import java.util.concurrent.atomic.AtomicBoolean;

public class EnterPhoneNumberHandler extends DataHandler {

    @FXML
    private TextField phoneNumberTextField;

    @FXML
    private ImageView nextButtonImg;

    @FXML
    private Label enterPhoneNumberLabel;

    @FXML
    private MenuButton selectCountryMenu;

    @FXML
    private Label countryCodeLabel;

    @FXML
    private MenuItem russianFederationMenuItem;

    @FXML
    private MenuItem belarusMenuItem;

    public EnterPhoneNumberHandler() {}

    @FXML
    private void handleKeyTyped(){
        if (selectCountryMenu.getText().equals(russianFederationMenuItem.getText())){
            phoneNumberTextField.addEventFilter(KeyEvent.KEY_TYPED, maxLength(10));
        }
        else if(selectCountryMenu.getText().equals(belarusMenuItem.getText())){
            phoneNumberTextField.addEventFilter(KeyEvent.KEY_TYPED, maxLength(9));
        }
    }

    @FXML
    private void handleConfirmNumberAction(){
        String phoneNumberStr = phoneNumberTextField.getCharacters().toString();
        if (selectCountryMenu.getText().equals(russianFederationMenuItem.getText())){
            phoneNumberStr = "7"+phoneNumberStr;
        }
        else if(selectCountryMenu.getText().equals(belarusMenuItem.getText())){
            phoneNumberStr = "375"+phoneNumberStr;
        }
        getUserStateRef().get().setPhoneNumber(phoneNumberStr);
        getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToEnterVerificationCodeWindowFXML);
        ((EnterVerificationCodeHandler) getEncryWindow().getCurrentController() )
                .setPhoneNumberLabelText( getUserStateRef().get().getPreparedPhoneNumber());
    }

    @FXML
    private void handlePhoneNumberAreaPressed(){
        handleNumberAccepted(nextButtonImg);
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

    @FXML
    private void showPrompt(){
        enterPhoneNumberLabel.setVisible(true);
    }

    @FXML
    private void hidePrompt(){
        enterPhoneNumberLabel.setVisible(false);
    }

    private void setCountryMenuItem(String country, String code, String promptText){
        nextButtonImg.setDisable(false);
        phoneNumberTextField.setDisable(false);
        selectCountryMenu.setText(country);
        countryCodeLabel.setVisible(true);
        countryCodeLabel.setText(code);
        phoneNumberTextField.setText("");
        phoneNumberTextField.setPromptText(promptText);
    }

    @FXML
    private void setRussiaDefault(){
        setCountryMenuItem(russianFederationMenuItem.getText(), "+7", "--- --- -- --");
    }

    @FXML
    private void setBelarusDefault(){
        setCountryMenuItem(belarusMenuItem.getText(), "+375", "-- --- -- --");
    }

    private EventHandler<KeyEvent> maxLength(final Integer i) {
        return arg0 -> {
            TextField tx = (TextField) arg0.getSource();
            if (tx.getText().length() >= i) {
                arg0.consume();
            }
        };
    }
}
