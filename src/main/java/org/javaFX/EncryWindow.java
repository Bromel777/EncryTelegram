package org.javaFX;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.javaFX.view.InputDataHandler;

import java.io.IOException;

public class EncryWindow extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;

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

    public void initBasicHandler(FXMLLoader loader){
        InputDataHandler controller = loader.getController();
        controller.setEncryWindow(this);
        controller.setStage(primaryStage);
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
        InputDataHandler controller = loader.getController();
        controller.setStage(primaryStage);
    }


    public Stage getPrimaryStage () {
            return primaryStage;
        }

    public BorderPane getRootLayout() {
        return rootLayout;
    }
}

