package org.javaFX.util.observers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import org.javaFX.controller.DataHandler;

public class JWindowObserver extends BasicObserver {

    public JWindowObserver(DataHandler controller) {
        super(controller);
    }

    protected Task<Object> createTask() {
        return new Task<Object>() {
            protected Object call() {
                Platform.runLater(()->{
                    getController().updateEncryWindow(getController().getEncryWindow());
                });
                return null;
            }
        };
    }

}