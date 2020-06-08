package org.javaFX.util.observers;

import javafx.concurrent.Task;
import org.javaFX.controller.handlers.DataHandler;

public class JTableObserver extends BasicObserver {

    public JTableObserver(DataHandler controller) {
        super(controller);
    }

    protected Task<Object> createTask() {
        return new Task<Object>() {
            protected Object call() {
                getController().updateEncryWindow(getController().getEncryWindow());
                return null;
            }
        };
    }

}