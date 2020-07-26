package org.javaFX.controller.impl.handler;

import javafx.util.Duration;
import org.javaFX.EncryWindow;
import org.javaFX.controller.DataHandler;

import org.javaFX.util.observers.JWindowObserver;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class CommunitiesWindowHandler extends DataHandler {

    private ScheduledExecutorService service;
    private Duration period = Duration.millis(10);


    public CommunitiesWindowHandler() {
        runDelayedInitialization();
    }

    private void runDelayedInitialization(){
        service = Executors.newSingleThreadScheduledExecutor();
        service.schedule(() -> insertDataToView(getEncryWindow()), 1000, TimeUnit.MILLISECONDS);
        initObserver();
    }

    private void initObserver(){
        setObserver(new JWindowObserver(this));
        getObserver().setPeriod(period);
        getObserver().start();
    }


    private void insertDataToView(EncryWindow encryWindow){
        super.setEncryWindow(encryWindow);
        initChatsTable();
    }

    @Override
    public void updateEncryWindow(EncryWindow encryWindow) {
        super.updateEncryWindow(encryWindow);
    }

    protected abstract void initChatsTable();

    protected void shutDownScheduledService(){
        if(!service.isShutdown())
            service.shutdown();
    }
}
