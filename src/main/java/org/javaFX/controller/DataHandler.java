package org.javaFX.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.javaFX.EncryWindow;
import org.javaFX.model.JUserState;
import org.javaFX.util.observers.BasicObserver;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public abstract class DataHandler {

    private static BasicObserver observer;
    @FXML
    private Stage stage;
    private EncryWindow encryWindow;
    private AtomicReference<JUserState> userStateRef;
    private BorderPane rootLayout;


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

    protected Stage createDialogByPathToFXML(FXMLLoader loader, String path){
        loader.setLocation(EncryWindow.class.getResource(path));
        Stage dialogStage = new Stage();
        try {
            AnchorPane startOverview = loader.load();
            Scene scene = new Scene(startOverview);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dialogStage;
    }

}
