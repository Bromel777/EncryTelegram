package org.javaFX.controller.impl.handler;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.encryfoundation.tg.javaIntegration.JavaInterMsg;
import org.encryfoundation.tg.javaIntegration.BackMsg;
import org.javaFX.EncryWindow;
import org.javaFX.controller.DataHandler;
import java.io.IOException;

public class RootLayoutHandler extends DataHandler {

    @FXML
    private MenuItem aboutMenuItem;

    @FXML
    private void createLocalCommunity(){
        terminateObserver();
        getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToCreateNewCommunityWindowFXML);
    }

    @FXML
    private void logOut(){
        BackMsg msg = new BackMsg.Logout();
        try {
            getUserStateRef().get().outQueue.put(msg);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToEnterPhoneNumberWindowFXML);
        if(getRootLayout().getTop().isVisible()) {
            getRootLayout().getTop().setDisable(true);
            getRootLayout().getTop().setVisible(false);
        }
    }

    @FXML
    private void closeWindow(){
        System.exit(0);
    }

    @FXML
    private void launchMainWindow(){
        terminateObserver();
        getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToChatsWindowFXML);
        ((ChatsWindowHandler)getEncryWindow().getCurrentController()).hideLeftPane();
    }

    @FXML
    private void showLocalCommunity(){
        terminateObserver();
        if (getUserStateRef().get().communities.size() == 0 ){
            getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToEmptyCommunitiesListWindowFXML);
        }
        else {
            getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToCommunitiesListWindowFXML);
        }
    }

    @FXML
    private void showInfoWindow(){
        launchDialog();
        aboutMenuItem.setDisable(true);
    }

    private void launchDialog(){
        FXMLLoader loader = new FXMLLoader();
        Stage dialogStage = createDialogByPathToFXML(loader, EncryWindow.pathToInfoDialogFXML);
        InfoDialogHandler controller = loader.getController();
        controller.setEncryWindow(getEncryWindow());
        controller.setDialogStage(dialogStage);
        controller.setUserStateRef(getUserStateRef());
        controller.setParentPageName(getEncryWindow().getCurrentWindowStr() );
        controller.setAboutMenuItem(aboutMenuItem);
        dialogStage.show();
    }

    private Stage createDialogByPathToFXML(FXMLLoader loader, String path){
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

    @Override
    public String toString() {
        return "RootLayoutHandler{}"+getEncryWindow()+"\t"+getRootLayout()+"\t"+getStage()+"\t"+getUserStateRef();
    }

    public MenuItem getAboutMenuItem() {
        return aboutMenuItem;
    }
}
