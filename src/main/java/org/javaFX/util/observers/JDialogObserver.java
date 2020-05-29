package org.javaFX.util.observers;

import javafx.concurrent.Task;
import org.javaFX.controller.DataHandler;
import org.javaFX.util.BasicObserver;

public class JDialogObserver extends BasicObserver {
    public JDialogObserver(DataHandler controller) {
        super(controller);
    }

    protected Task<Object> createTask() {
        return new Task<Object>() {
            protected Object call() {
                //getController().updateEncryWindow(getController().getEncryWindow());
                return null;
            }
        };
    }
}
