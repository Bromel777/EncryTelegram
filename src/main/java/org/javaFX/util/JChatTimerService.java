package org.javaFX.util;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import org.javaFX.controller.handlers.DataHandler;

public class JChatTimerService extends ScheduledService<Object > {

    private final DataHandler controller;

    public JChatTimerService(DataHandler controller) {
        this.controller = controller;
    }

    protected Task<Object> createTask() {
        return new Task<Object>() {
            protected Object call() {
                controller.updateEncryWindow(controller.getEncryWindow());
                return null;
            }
        };
    }
}