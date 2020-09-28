package org.javaFX.controller.impl.handler;

import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.javaFX.EncryWindow;

import org.javaFX.model.JLocalCommunity;
import org.javaFX.model.nodes.VBoxCommunityCell;
import org.javaFX.model.nodes.VBoxContactCell;
import org.javaFX.util.KeyboardHandler;

import java.io.IOException;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;

public class LocalCommunitiesWindowHandler extends CommunitiesWindowHandler {

    @FXML
    private ListView<VBoxCommunityCell> communitiesListView;

    @FXML
    private TextField searchCommunityTextField;

    @FXML
    private TextField privateChatNameTestField;

    @FXML
    private Label descriptionLabel;

    @FXML
    private Separator blueSeparator;

    @FXML
    private Label notFoundInfoLabel;

    @FXML
    private ImageView searchImg;

    public LocalCommunitiesWindowHandler() {
        super();
    }

    @Override
    public void updateEncryWindow(EncryWindow encryWindow) {
        for(VBoxCommunityCell cell : communitiesListView.getItems()){
            cell.setSeparatorLineSize(blueSeparator.getWidth()- 40);
        }
        super.setEncryWindow(encryWindow);
    }

    private ObservableList<VBoxCommunityCell> getObservableCommunityList(){
        final String searchingStr = searchCommunityTextField.getText().trim();
        ObservableList<VBoxCommunityCell> observableList = getFilteredList(initTableBySubstr(searchingStr), searchingStr);
        if(observableList.size() == 0 ){
            notFoundInfoLabel.setVisible(true);
        }
        return observableList;
    }

    private ObservableList<VBoxCommunityCell> getFilteredList(ObservableList<VBoxCommunityCell> rawList, final String searchString ){
        rawList.sort( Comparator.comparing(contactCell -> ((VBoxCommunityCell)contactCell)
                .getCurrentCommunity().getCommunityName().indexOf(searchString.toLowerCase())));
        return rawList;
    }

    @Override
    protected void initChatsTable(){
        communitiesListView.setItems(getObservableCommunityList());
        shutDownScheduledService();
    }

    private void refreshColors(VBoxCommunityCell activeCell){
        for(VBoxCommunityCell cell: communitiesListView.getItems()){
            cell.resetPaneColor();
        }
        activeCell.updatePaneColor();
    }

    @FXML
    private void onClick(){
        VBoxCommunityCell activeCell = communitiesListView.getSelectionModel().getSelectedItem();
        JLocalCommunity localCommunity = activeCell.getCurrentCommunity();
        refreshColors(activeCell);
        launchDialog(localCommunity);
    }

    @FXML
    private void toChatsWindow(){
        getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToChatsWindowFXML);
    }

    @FXML
    private void searchContactByKeyboard(){
        AtomicBoolean keysPressed = KeyboardHandler.handleEnterPressed(searchCommunityTextField);
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if ( keysPressed.get() ) {
                    findContact();
                }
            }
        }.start();
    }

    private void findContact(){
        final String searchingStr = searchCommunityTextField.getText().trim();
        communitiesListView.setItems(initTableBySubstr(searchingStr));
    }

    private ObservableList<VBoxCommunityCell> initTableBySubstr(String searchingStr){
        ObservableList<VBoxCommunityCell> observableList = FXCollections.observableArrayList();
        getUserStateRef().get().communities
                .stream()
                .filter(item -> item.getCommunityName().toLowerCase().contains(searchingStr.toLowerCase()) )
                .forEach(community -> observableList.add(new VBoxCommunityCell(community)));
        if(observableList.size() == 0 ){
            notFoundInfoLabel.setVisible(true);
        }
        else {
            notFoundInfoLabel.setVisible(false);
        }
        return observableList;
    }

    private void launchDialog(JLocalCommunity localCommunity){
        FXMLLoader loader = new FXMLLoader();
        Stage dialogStage = createDialogByPathToFXML(loader, EncryWindow.pathToSingleCommunityDialogFXML);
        SingleCommunityDialogHandler controller = loader.getController();
        controller.setEncryWindow(getEncryWindow());
        controller.setDialogStage(dialogStage);
        controller.setLocalCommunity(localCommunity);
        controller.setUserStateRef(getUserStateRef());
        final String secretChatName = privateChatNameTestField.getText();
        if(!secretChatName.isEmpty()){
            controller.setSecretChatNameText(secretChatName);
        }
        else{
            controller.setSecretChatNameText(localCommunity.getCommunityName());
        }
        dialogStage.show();
    }

    protected Stage createDialogByPathToFXML(FXMLLoader loader, String path){
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


    @FXML
    private void handleSearchCommunityKeyTyped(){
        searchCommunityTextField.addEventFilter(KeyEvent.KEY_TYPED, KeyboardHandler.maxLengthHandler(40));
    }

    @FXML
    private void handlePrivateChatKeyTyped(){
        privateChatNameTestField.addEventFilter(KeyEvent.KEY_TYPED, KeyboardHandler.maxLengthHandler(40));
    }

    @FXML
    private void handleSearchImg(){
        searchImg.setVisible(false);
        searchCommunityTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue) {
                if(searchCommunityTextField.getText().length() == 0){
                    searchImg.setVisible(true);
                }
            }
        });
    }

}