package org.javaFX.util.observers;

import javafx.concurrent.ScheduledService;
import org.javaFX.controller.handlers.DataHandler;

public abstract class BasicObserver extends ScheduledService<Object> {

    private final DataHandler controller;

    public BasicObserver(DataHandler controller) {
        this.controller = controller;
    }

    public DataHandler getController() {
        return controller;
    }
}
