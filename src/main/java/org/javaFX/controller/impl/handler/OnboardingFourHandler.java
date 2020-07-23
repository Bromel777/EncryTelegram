package org.javaFX.controller.impl.handler;

import org.encryfoundation.tg.javaIntegration.BackMsg;
import org.encryfoundation.tg.javaIntegration.FrontMsg;
import org.javaFX.EncryWindow;
import org.javaFX.controller.OnboardingHandler;

public class OnboardingFourHandler extends OnboardingHandler {

    @Override
    protected void handleNextOnboardingAction() {
        try {
            FrontMsg nextStep = EncryWindow.state.get().inQueue.take();
            if (nextStep.code() == FrontMsg.Codes$.MODULE$.loadPass()) {
                getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToEnterPasswordWindowFXML);
            } else if (nextStep.code() == FrontMsg.Codes$.MODULE$.loadVc()) {
                getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToEnterVerificationCodeWindowFXML);
            } else if (nextStep.code() == FrontMsg.Codes$.MODULE$.loadPhone()) {
                getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToEnterPhoneNumberWindowFXML);
            } else if (nextStep.code() == FrontMsg.Codes$.MODULE$.loadChats()) {
                getEncryWindow().launchWindowByPathToFXML(
                        EncryWindow.pathToChatsWindowFXML, EncryWindow.afterInitializationWidth, EncryWindow.afterInitializationHeight
                );
            }
            //todo: remove usage of handleNextOnboardingAction() here
            else if (nextStep.code() == FrontMsg.Codes$.MODULE$.loadUnboarding()) {
                handleNextOnboardingAction();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
