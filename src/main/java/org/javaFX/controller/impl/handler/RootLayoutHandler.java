package org.javaFX.controller.impl.handler;

import javafx.fxml.FXML;
import org.javaFX.EncryWindow;
import org.javaFX.controller.DataHandler;
import org.javaFX.model.JTableEntity;
import org.javaFX.util.observers.BasicObserver;

public class RootLayoutHandler extends DataHandler {

    private void terminateObserver(){
        BasicObserver observer = getObserver();
        if( observer != null){
            observer.cancel();
        }
    }

    @FXML
    private void createLocalCommunity(){
        terminateObserver();
        JTableEntity.resetRowNumber();
        getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToCreateNewCommunityWindowFXML);
    }

    @FXML
    private void closeWindow(){
        System.exit(0);
    }

    @FXML
    private void launchMainWindow(){
        terminateObserver();
        JTableEntity.resetRowNumber();
        getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToChatsWindowFXML);
    }

    @FXML
    private void showLocalCommunity(){
        terminateObserver();
        JTableEntity.resetRowNumber();
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
