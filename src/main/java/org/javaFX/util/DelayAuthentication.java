package org.javaFX.util;

import javafx.application.Platform;
import org.javaFX.EncryWindow;

import java.util.concurrent.atomic.AtomicInteger;

public class DelayAuthentication extends Thread{
    private long delayMilliseconds ;
    private EncryWindow encryWindow;

    public DelayAuthentication(EncryWindow encryWindow, long delayMilliseconds) {
        this.encryWindow = encryWindow;
        this.delayMilliseconds = delayMilliseconds;
    }

    @Override
    public void run() {
        Runnable updater = () -> {
            if (EncryWindow.state.get().isAuth()) {
                encryWindow.launchMainWindow();
            } else {
                encryWindow.launchAuthenticationWindow();
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
