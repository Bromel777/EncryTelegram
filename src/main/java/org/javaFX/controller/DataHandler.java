package org.javaFX.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.javaFX.EncryWindow;
import org.javaFX.model.JUserState;
import org.javaFX.util.observers.BasicObserver;

import java.util.concurrent.atomic.AtomicReference;

public abstract class DataHandler {

    private static BasicObserver observer;
    @FXML
    private Stage stage;
    private EncryWindow encryWindow;
    private AtomicReference<JUserState> userStateRef;
    private BorderPane rootLayout;

    public DataHandler() {
        terminateObserver();
    }

    public BorderPane getRootLayout() {
        return rootLayout;
    }

    public void setRootLayout(BorderPane rootLayout) {
        this.rootLayout = rootLayout;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public EncryWindow getEncryWindow() {
        return encryWindow;
    }

    public void setEncryWindow(EncryWindow encryWindow) {
        this.encryWindow = encryWindow;
    }

    public AtomicReference<JUserState> getUserStateRef() {
        return userStateRef;
    }

    public void setUserStateRef(AtomicReference<JUserState> userStateRef) {
        this.userStateRef = userStateRef;
    }

    public void updateEncryWindow(EncryWindow encryWindow){
        this.setEncryWindow(encryWindow);
    }

    public BasicObserver getObserver() {
        return observer;
    }

    public void setObserver(BasicObserver newObserver) {
        observer = newObserver;
    }

    @Override
    public String toString() {
        return this.getClass().toString();
    }

    protected void terminateObserver(){
        BasicObserver observer = getObserver();
        if( observer != null){
            observer.cancel();
        }
    }

}
