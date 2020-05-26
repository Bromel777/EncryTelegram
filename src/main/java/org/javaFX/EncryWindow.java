package org.javaFX;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.javaFX.controller.DataHandler;
import org.javaFX.controller.MainWindowDataHandler;
import org.javaFX.model.JUserState;
import org.javaFX.controller.InputDataHandler;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
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

    public void initRootLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(EncryWindow.class.getResource("view/rootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateControllerState(FXMLLoader loader){
        InputDataHandler controller = loader.getController();
        controller.setEncryWindow(this);
        controller.setStage(primaryStage);
        controller.setUserStateRef(this.state);
    }

    public void launchStartWindow() {
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(EncryWindow.class.getResource("view/startWindowSettings.fxml"));
            AnchorPane startOverview = (AnchorPane) loader.load();
            rootLayout.setCenter(startOverview);
            initBasicHandler(loader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initBasicHandler(FXMLLoader loader){
        InputDataHandler controller = loader.getController();
        controller.setEncryWindow(this);
        controller.setStage(primaryStage);
        controller.setUserStateRef(this.state);
    }

    public void launchMainWindow() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(EncryWindow.class.getResource("view/mainWindowSettings.fxml"));
            AnchorPane mainOverView = (AnchorPane) loader.load();
            rootLayout.setCenter(mainOverView);
            initMainDataHandler(loader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initMainDataHandler(FXMLLoader loader){
        System.out.println(loader.getController().getClass());
        MainWindowDataHandler controller = loader.getController();
        controller.setEncryWindow(this);
        controller.setStage(primaryStage);
        controller.setUserStateRef(this.state);
    }


    public Stage getPrimaryStage () {
            return primaryStage;
        }

    public BorderPane getRootLayout() {
        return rootLayout;
    }



    public ObservableList<String> getObservableChatListTest(){
        ObservableList<String> observableChatList = FXCollections.observableArrayList();
        String chat1 = "chat1";
        String chat2 = "chat2";
        String chat3 = "chat3";
        observableChatList.add(chat1);
        observableChatList.add(chat2);
        observableChatList.add(chat3);
        for(String s : observableChatList){
            System.out.println(s);
        }
        return observableChatList;
    }
}

