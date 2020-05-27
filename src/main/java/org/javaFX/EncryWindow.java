package org.javaFX;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.javaFX.controller.*;
import org.javaFX.model.JUserState;
import org.javaFX.util.DelayAuthentication;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public class EncryWindow extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;

    public static AtomicReference<JUserState> state = new AtomicReference<>(new JUserState());

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        initBasicFields(primaryStage);
        initRootLayout();
        launchStartWindow();
        primaryStage.show();
    }

    private void initBasicFields(Stage primaryStage){
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("ETC");
        this.primaryStage.getIcons().add(new Image("file:src/main/resources/images/logo.png"));
    }

    private void initRootLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(EncryWindow.class.getResource("view/rootLayout.fxml"));
            rootLayout = loader.load();
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void launchStartWindow(){
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(EncryWindow.class.getResource("view/startWindow.fxml"));
            AnchorPane startOverview = loader.load();
            rootLayout.setCenter(startOverview);
            initStartWindow(loader);
            DelayAuthentication delayAuthentication = new DelayAuthentication(this, 5000);
            delayAuthentication.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initStartWindow(FXMLLoader loader){
        DataHandler controller = loader.getController();
        ImageView loadingGif = new ImageView(
                new Image(
                        this.getClass().getResource("/images/loading.gif").toExternalForm()
                )
        );
        loadingGif.toFront();
        ((StartWindowHandler) controller).setLoadingGif(loadingGif);
        controller.setEncryWindow(this);
        controller.setStage(primaryStage);
    }

    public void launchAuthenticationWindow() {
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(EncryWindow.class.getResource("view/authenticationWindow.fxml"));
            AnchorPane startOverview = loader.load();
            rootLayout.setCenter(startOverview);
            initAuthenticationHandler(loader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initAuthenticationHandler(FXMLLoader loader){
        DataHandler controller = loader.getController();
        controller.setUserStateRef(state);
        controller.setEncryWindow(this);
        controller.setStage(primaryStage);
    }

    public void launchMainWindow() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(EncryWindow.class.getResource("view/mainWindow.fxml"));
            AnchorPane mainOverView = loader.load();
            rootLayout.setCenter(mainOverView);
            initMainDataHandler(loader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initMainDataHandler(FXMLLoader loader){
        DataHandler controller = loader.getController();
        controller.setUserStateRef(state);
        controller.setEncryWindow(this);
        controller.setStage(primaryStage);

        //chatListObserve(controller);
    }

    public void updateControllerState(FXMLLoader loader){
        InputDataHandler controller = loader.getController();
        controller.setEncryWindow(this);
        controller.setStage(primaryStage);
        controller.setUserStateRef(state);
    }

}