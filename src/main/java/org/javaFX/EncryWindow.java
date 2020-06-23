package org.javaFX;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.javaFX.controller.DataHandler;
import org.javaFX.model.JUserState;
import org.javaFX.util.DelayAuthentication;

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
    public final static String pathToCreateCommunityWindowFXML = "view/localCommunityWindow.fxml";
    public final static String pathToLocalCommunityNameDialogFXML = "view/localCommunityNameDialog.fxml";
    public final static String pathToLocalShowCommunitiesWindowFXML = "view/showCreatedLocalCommunitiesWindow.fxml";
    public final static String pathToCreateSubmitPrivateChatFXML = "view/createPrivateChatDialog.fxml";
    private final static String pathToRootLayout = "view/rootLayout.fxml";

    private DataHandler rootLayoutHandler;

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

    public void launchStartWindow(){
        launchWindowByPathToFXML(pathToStartWindowFXML);
        new DelayAuthentication(this, 5000).start();
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
            initRootController(loader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initRootController(FXMLLoader loader){
        rootLayoutHandler = loader.getController();
        Scene scene = new Scene(rootLayout);
        primaryStage.setScene(scene);
        //primaryStage.setResizable(false);
        updateController(loader);
        primaryStage.show();
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

    private void updateController(FXMLLoader loader){
        DataHandler controller = loader.getController();
        controller.setUserStateRef(state);
        controller.setEncryWindow(this);
        controller.setStage(primaryStage);
        controller.setRootLayout(rootLayout);
    }

}