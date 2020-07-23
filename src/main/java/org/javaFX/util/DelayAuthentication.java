package org.javaFX.util;

import javafx.application.Platform;
import org.encryfoundation.tg.javaIntegration.BackMsg;
import org.encryfoundation.tg.javaIntegration.FrontMsg;
import org.javaFX.EncryWindow;
import org.javaFX.controller.impl.handler.EnterVerificationCodeHandler;

import java.util.concurrent.atomic.AtomicInteger;

public class DelayAuthentication extends Thread {
    private final long delayMilliseconds ;
    private final EncryWindow encryWindow;

    public DelayAuthentication(EncryWindow encryWindow, long delayMilliseconds) {
        this.encryWindow = encryWindow;
        this.delayMilliseconds = delayMilliseconds>=0 ? delayMilliseconds: delayMilliseconds*(-1) ;
    }

    @Override
    public void run() {
        Runnable updater = () -> {
            try {
                FrontMsg nextStep = EncryWindow.state.get().inQueue.take();
                if (nextStep.code() == FrontMsg.Codes$.MODULE$.loadPass()) {
                    encryWindow.launchWindowByPathToFXML(EncryWindow.pathToEnterPasswordWindowFXML);
                } else if (nextStep.code() == FrontMsg.Codes$.MODULE$.loadVc()) {
                    encryWindow.launchWindowByPathToFXML(EncryWindow.pathToEnterVerificationCodeWindowFXML);
                } else if (nextStep.code() == FrontMsg.Codes$.MODULE$.loadPhone()) {
                    encryWindow.launchWindowByPathToFXML(EncryWindow.pathToEnterPasswordWindowFXML);
                } else if (nextStep.code() == FrontMsg.Codes$.MODULE$.loadUnboarding()) {
                    encryWindow.launchWindowByPathToFXML(EncryWindow.pathToOnboardingOneFXML);
                } else if (nextStep.code() == FrontMsg.Codes$.MODULE$.loadChats()) {
                    encryWindow.launchWindowByPathToFXML(
                            EncryWindow.pathToChatsWindowFXML, EncryWindow.afterInitializationWidth, EncryWindow.afterInitializationHeight
                    );
                } else {
                    System.out.println("Take in delay auth: " + nextStep);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        AtomicInteger atomicInteger = new AtomicInteger(0);
        while (atomicInteger.get() <= delayMilliseconds / 1000 ) {
            try {
                Thread.sleep(1000);
                atomicInteger.incrementAndGet();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        Platform.runLater(updater);
    }
}
