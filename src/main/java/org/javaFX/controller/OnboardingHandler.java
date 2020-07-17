package org.javaFX.controller;

import javafx.fxml.FXML;
import org.javaFX.EncryWindow;

public abstract class OnboardingHandler extends DataHandler{

    @FXML
    protected abstract void handleNextOnboardingAction();

    @FXML
    protected void toOnboarding1(){
        getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToOnboardingOneFXML);
    }

    @FXML
    protected void toOnboarding2(){
        getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToOnboardingTwoFXML);
    }

    @FXML
    protected void toOnboarding3(){
        getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToOnboardingThreeFXML);
    }

    @FXML
    protected void toOnboarding4(){
        getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToOnboardingFourFXML);
    }
}
