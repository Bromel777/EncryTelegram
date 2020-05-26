package org.javaFX.util;

import org.javaFX.EncryWindow;
import org.javaFX.controller.MainWindowDataHandler;

public class Observer extends Thread{
    private MainWindowDataHandler mainWindowDataHandler;
    private EncryWindow encryWindow;

    public Observer(MainWindowDataHandler mainWindowDataHandler, EncryWindow encryWindow) {
        this.mainWindowDataHandler = mainWindowDataHandler;
        this.encryWindow = encryWindow;
    }

    @Override
    public void run() {
        super.run();
        mainWindowDataHandler.setEncryWindow(encryWindow);
    }
}
