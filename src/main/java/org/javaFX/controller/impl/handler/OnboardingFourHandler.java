package org.javaFX.controller.impl.handler;

import javafx.fxml.FXML;
import org.javaFX.EncryWindow;
import org.javaFX.controller.DataHandler;
import org.javaFX.util.DelayAuthentication;

public class OnboardingFourHandler extends DataHandler {

    @FXML
    private void handleNextOnboardingAction(){
        new DelayAuthentication(getEncryWindow(), 5000).start();
        getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToStartWindowFXML);
    }
}
