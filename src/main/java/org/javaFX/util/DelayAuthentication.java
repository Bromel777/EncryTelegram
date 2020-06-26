package org.javaFX.util;

import javafx.application.Platform;
import org.javaFX.EncryWindow;

import java.util.concurrent.atomic.AtomicInteger;

public class DelayAuthentication extends Thread{
    private final long delayMilliseconds ;
    private final EncryWindow encryWindow;

    public DelayAuthentication(EncryWindow encryWindow, long delayMilliseconds) {
        this.encryWindow = encryWindow;
        this.delayMilliseconds = delayMilliseconds>=0 ? delayMilliseconds: delayMilliseconds*(-1) ;
    }

    @Override
    public void run() {
        Runnable updater = () -> {
            if (EncryWindow.state.get().isAuth()) {
                encryWindow.launchWindowByPathToFXML(EncryWindow.pathToMainWindowFXML);
            } else {
                encryWindow.launchWindowByPathToFXML(EncryWindow.pathToEnterPhoneNumberWindowFXML);
                //encryWindow.launchWindowByPathToFXML(EncryWindow.pathToAuthenticationWindowFXML);
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
