package org.javaFX;

import javafx.application.Application;
import javafx.concurrent.ScheduledService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.javaFX.controller.*;
import org.javaFX.model.JUserState;
import org.javaFX.util.DelayAuthentication;
import org.javaFX.util.JChatTimerService;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public class EncryWindow extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;

    private final String windowTitle = "ETC";
    private final String pathToLogoImage = "file:src/main/resources/images/logo.png";
    public final static String pathToStartWindowFXML = "view/startWindow.fxml";
    public final static String pathToMainWindowFXML = "view/mainWindow.fxml";
    public final static String pathToAuthenticationWindowFXML = "view/authenticationWindow.fxml";
    private final static String pathToRootLayout = "view/rootLayout.fxml";

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
        this.primaryStage.setTitle(windowTitle);
        this.primaryStage.getIcons().add(new Image(pathToLogoImage));
    }

    private void initRootLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(EncryWindow.class.getResource(pathToRootLayout));
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
        launchWindowByPathToFXML(pathToStartWindowFXML);
        DelayAuthentication delayAuthentication = new DelayAuthentication(this, 5000);
        delayAuthentication.start();
    }

    private void updateController(FXMLLoader loader){
        DataHandler controller = loader.getController();
        controller.setUserStateRef(state);
        controller.setEncryWindow(this);
        controller.setStage(primaryStage);
    }

    public void launchWindowByPathToFXML(String pathToTemplate){
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(EncryWindow.class.getResource(pathToTemplate));
            AnchorPane startOverview = loader.load();
            rootLayout.setCenter(startOverview);
            updateController(loader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void chatListObserve(DataHandler controller){
        ScheduledService<Object> service = new JChatTimerService(controller);
        service.setPeriod(Duration.seconds(3));
        service.start();
    }

}