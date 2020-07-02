package org.javaFX.controller.impl.handler;

import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import org.encryfoundation.tg.javaIntegration.JavaInterMsg;
import org.javaFX.EncryWindow;

import org.javaFX.model.JLocalCommunity;
import org.javaFX.model.nodes.VBoxCommunityCell;
import org.javaFX.model.nodes.VBoxContactCell;
import org.javaFX.util.InfoContainer;
import org.javaFX.util.KeyboardHandler;

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
        getUserStateRef().get().communities.forEach(community ->
                observableList.add(new VBoxCommunityCell(
                        new JLocalCommunity(community, InfoContainer.getSizeByName(community)) ) ) );
        return observableList;
    }

    @Override
    protected void initChatsTable(){
        communitiesListView.setItems(getObservableCommunityList());
        shutDownScheduledService();
    }

    @FXML
    private void createPrivateChat() throws InterruptedException {
        if(!privateChatNameTestField.getText().isEmpty()){
            JavaInterMsg msg = new JavaInterMsg.CreatePrivateGroupChat(privateChatNameTestField.getText());
            getUserStateRef().get().msgsQueue.put(msg);
            getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToChatsWindowFXML);
        }
        else{
            privateChatNameTestField.setFocusTraversable(true);
            descriptionLabel.setTextFill(Color.RED);
        }
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

}