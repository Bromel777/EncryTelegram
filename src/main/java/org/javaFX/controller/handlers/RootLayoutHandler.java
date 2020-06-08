package org.javaFX.controller.handlers;

import javafx.fxml.FXML;
import org.javaFX.EncryWindow;
import org.javaFX.util.observers.BasicObserver;

public class RootLayoutHandler extends DataHandler {

    @FXML
    private void closeWindow(){
        getStage().close();
    }

    private void terminateObserver(){
        BasicObserver observer = getObserver();
        if( observer != null){
            observer.cancel();
        }
    }

    @FXML
    private void createLocalCommunity(){
        terminateObserver();
        getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToCreateCommunityWindowFXML);
    }

    @FXML
    private void updateLocalCommunity(){
        terminateObserver();
        System.out.println("update local community");
    }
    @FXML
    private void deleteLocalCommunity(){
        terminateObserver();
        System.out.println("delete local community");
    }
    @FXML
    private void showLocalCommunity(){
        terminateObserver();
        System.out.println("show local community");
    }

    @Override
    public String toString() {
        return "RootLayoutHandler{}"+getEncryWindow()+"\t"+getRootLayout()+"\t"+getStage()+"\t"+getUserStateRef();
    }
}
