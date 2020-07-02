package org.javaFX.controller.impl.handler;

import javafx.fxml.FXML;
import org.javaFX.EncryWindow;
import org.javaFX.controller.DataHandler;

public class EmptyCommunitiesListHandler extends DataHandler {

    @FXML
    private void toCreateCommunityWindow(){
        getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToCreateNewCommunityWindowFXML);
    }
}
