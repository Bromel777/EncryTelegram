package org.javaFX.controller.impl.handler;

import javafx.fxml.FXML;
import org.encryfoundation.tg.javaIntegration.JavaInterMsg;
import org.javaFX.EncryWindow;
import org.javaFX.controller.DataHandler;

public class RootLayoutHandler extends DataHandler {

    @FXML
    private void createLocalCommunity(){
        terminateObserver();
        getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToCreateNewCommunityWindowFXML);
    }

    //TODO If I got it right You told me to send some kind of this message to the queue, but i couldn't find
    // appropriate method to invoke

    @FXML
    private void logOut(){
        /*
        getUserStateRef().get().msgsQueue.put(new JavaInterMsg.logOut());
        */
        getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToEnterPhoneNumberWindowFXML);
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
