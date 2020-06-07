package org.javaFX.controller.handlers;

import javafx.fxml.FXML;

public class RootLayoutHandler extends DataHandler {

    @FXML
    private void closeWindow(){
        System.out.println("close window");
        getStage().close();
    }
    @FXML
    private void createLocalCommunity(){
        System.out.println("create local community");
    }
    @FXML
    private void updateLocalCommunity(){
        System.out.println("create local community");
    }
    @FXML
    private void deleteLocalCommunity(){
        System.out.println("create local community");
    }
    @FXML
    private void showLocalCommunity(){
        System.out.println("create local community");
    }
}
