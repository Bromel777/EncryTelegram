package org.javaFX.controller.impl.handler;

import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.javaFX.EncryWindow;

import org.javaFX.model.JLocalCommunity;
import org.javaFX.model.nodes.VBoxCommunityCell;
import org.javaFX.util.InfoContainer;
import org.javaFX.util.KeyboardHandler;

import java.io.IOException;
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
        ObservableList<VBoxCommunityCell> observableList = FXCollections.observableArrayList();
        getUserStateRef().get().communities.forEach(community -> observableList.add(new VBoxCommunityCell(community)));
        return observableList;
    }

    @Override
    protected void initChatsTable(){
        communitiesListView.setItems(getObservableCommunityList());
        shutDownScheduledService();
    }

    @FXML
    private void onClick(){
        JLocalCommunity localCommunity = communitiesListView.getSelectionModel().getSelectedItem().getCurrentCommunity();
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
        communitiesListView.getItems().stream()
                .filter(item -> item.getCurrentCommunity().getCommunityName().toLowerCase().contains(searchingStr.toLowerCase()) )
                .findAny()
                .ifPresent(item -> {
                    communitiesListView.getSelectionModel().select(item);
                    communitiesListView.scrollTo(item);
                });
    }

    private void launchDialog(JLocalCommunity localCommunity){
        FXMLLoader loader = new FXMLLoader();
        Stage dialogStage = createDialogByPathToFXML(loader, EncryWindow.pathToSingleCommunityDialogFXML);
        SingleCommunityDialogHandler controller = loader.getController();
        controller.setEncryWindow(getEncryWindow());
        controller.setDialogStage(dialogStage);
        controller.setLocalCommunity(localCommunity);
        controller.setUserStateRef(getUserStateRef());
        controller.setSecretChatNameText(localCommunity.getCommunityName());
        controller.setParentPageName(EncryWindow.pathToChatsWindowFXML);
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


}