package org.javaFX.controller.impl.handler;

import org.javaFX.EncryWindow;
import org.javaFX.controller.OnboardingHandler;

public class OnboardingFourHandler extends OnboardingHandler {

    @Override
    protected void handleNextOnboardingAction() {
        getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToEnterPhoneNumberWindowFXML);
    }
}
