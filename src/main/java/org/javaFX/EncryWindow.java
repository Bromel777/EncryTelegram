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

    public final static int afterInitializationWidth = 920;
    public final static int afterInitializationHeight = 645;

    private Stage primaryStage;
    private BorderPane rootLayout;

    private final String windowTitle = "ENCRY Telegram Client";
    private final String pathToLogoImage = "file:/images/logo.png";

    private final static String pathToRootLayout = "view/rootLayout.fxml";

    public final static String pathToOnboardingOneFXML = "view/onBoarding1.fxml";
    public final static String pathToOnboardingTwoFXML = "view/onBoarding2.fxml";
    public final static String pathToOnboardingThreeFXML = "view/onBoarding3.fxml";
    public final static String pathToOnboardingFourFXML = "view/onBoarding4.fxml";

    public final static String pathToEnterPhoneNumberWindowFXML = "view/enterPhoneNumberWindow.fxml";
    public final static String pathToEnterVerificationCodeWindowFXML = "view/enterVerificationCodeWindow.fxml";
    public final static String pathToEnterPasswordWindowFXML = "view/enterPasswordWindow.fxml";
    public final static String pathToChatsWindowFXML = "view/chatsWindow.fxml";
    public final static String pathToCreateNewCommunityWindowFXML = "view/createNewLocalCommunity.fxml";
    public final static String pathToEmptyCommunitiesListWindowFXML = "view/emptyCommunitiesListWindow.fxml";
    public final static String pathToCommunitiesListWindowFXML = "view/communitiesListWindow.fxml";

    public final static String pathToSingleCommunityDialogFXML = "view/singleCommunityDialog.fxml";
    public final static String pathToInfoDialogFXML = "view/infoDialog.fxml";

    public final static String pathToStartWindowFXML = "view/startWindow.fxml";


    private String currentWindowStr ;

    private static String userPhoneNumber;

    private DataHandler rootLayoutHandler;

    private DataHandler currentController;

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
        new DelayAuthentication(this, 5000).start();
        this.launchWindowByPathToFXML(EncryWindow.pathToStartWindowFXML);
    }

    private void initBasicFields(Stage primaryStage){
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle(windowTitle);
        this.primaryStage.getIcons().add(new Image(pathToLogoImage));
        this.primaryStage.setMinWidth(620);
        this.primaryStage.setMinHeight(600);
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
        updateController(loader);
        primaryStage.show();
    }

    public void launchWindowByPathToFXML(String pathToTemplate){
        try{
            FXMLLoader loader = new FXMLLoader();
            currentWindowStr = pathToTemplate;
            loader.setLocation(EncryWindow.class.getResource(pathToTemplate));
            AnchorPane startOverview = loader.load();
            rootLayout.setCenter(startOverview);
            updateController(loader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void launchWindowByPathToFXML(String pathToTemplate, int newWidth, int newHeight){
        try{
            FXMLLoader loader = new FXMLLoader();
            currentWindowStr = pathToTemplate;
            loader.setLocation(EncryWindow.class.getResource(pathToTemplate));
            AnchorPane startOverview = loader.load();
            rootLayout.setCenter(startOverview);
            primaryStage.setHeight(newHeight);
            primaryStage.setWidth(newWidth);
            updateController(loader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateController(FXMLLoader loader){
        currentController = loader.getController();
        currentController.setUserStateRef(state);
        currentController.setEncryWindow(this);
        currentController.setStage(primaryStage);
        currentController.setRootLayout(rootLayout);
    }

    public DataHandler getCurrentController(){
        if (currentController != null){
            return currentController;
        }
        return null;
    }

    public Stage getPrimaryStage(){
        return primaryStage;
    }

    public static String getUserPhoneNumber(){
        return userPhoneNumber;
    }

    public static void setUserPhoneNumber(String phoneNumber){
        if(userPhoneNumber == null){
            userPhoneNumber = phoneNumber;
        }
    }

    public String getCurrentWindowStr() {
        return currentWindowStr;
    }
}