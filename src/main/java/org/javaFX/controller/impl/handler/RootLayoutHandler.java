package org.javaFX.controller.impl.handler;

import javafx.fxml.FXML;
import org.encryfoundation.tg.javaIntegration.BackMsg;
import org.javaFX.EncryWindow;
import org.javaFX.controller.DataHandler;

public class RootLayoutHandler extends DataHandler {

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

    @Override
    public String toString() {
        return "RootLayoutHandler{}"+getEncryWindow()+"\t"+getRootLayout()+"\t"+getStage()+"\t"+getUserStateRef();
    }
}
