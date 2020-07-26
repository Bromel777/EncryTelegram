package org.javaFX.controller.impl.handler;

import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import org.drinkless.tdlib.TdApi;
import org.encryfoundation.tg.javaIntegration.BackMsg;
import org.javaFX.EncryWindow;
import org.javaFX.model.JLocalCommunity;
import org.javaFX.model.JSingleContact;
import org.javaFX.model.nodes.VBoxContactCell;
import org.javaFX.util.InfoContainer;
import org.javaFX.util.KeyboardHandler;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class CreateNewLocalCommunityHandler extends CommunitiesWindowHandler {

    @FXML
    private ListView<VBoxContactCell> contactsListView;

    @FXML
    private TextField newCommunityNameTextField;

    @FXML
    private TextField searchContactTextField;

    @FXML
    private Label communityNameLabel;

    @FXML
    private Separator blueSeparator;

    public CreateNewLocalCommunityHandler() {
        super();
    }

    @Override
    public void updateEncryWindow(EncryWindow encryWindow) {
        for(VBoxContactCell cell : contactsListView.getItems()){
            cell.setSeparatorLineSize(blueSeparator.getWidth()- 40);
        }
        super.updateEncryWindow(encryWindow);
    }

    private ObservableList<VBoxContactCell> getObservableUserList(){
        ObservableList<VBoxContactCell> observableChatList = FXCollections.observableArrayList();
        for(Long jUserId: getUserStateRef().get().getUsersMap().keySet()){
            TdApi.User user = getUserStateRef().get().getUsersMap().get(jUserId);
            if(!user.phoneNumber.isEmpty())
                observableChatList.add(
                        new VBoxContactCell(
                                new JSingleContact(user.firstName, user.lastName, user.phoneNumber, (long)user.id)));
        }
        return observableChatList;
    }

    @Override
    protected void initChatsTable(){
        ObservableList<VBoxContactCell> t = getObservableUserList();
        contactsListView.setItems(t);
        shutDownScheduledService();
    }

    @FXML
    private void changeCheckBoxValue() {
        JSingleContact communityMember = contactsListView.getSelectionModel().getSelectedItem().getCurrentContact();
        if(communityMember.getUserId() > 0L ){
            boolean isChosen = communityMember.isChosen();
            communityMember.setChosen(!isChosen);
            contactsListView.getSelectionModel().getSelectedItem().changeCheckboxStatus();
        }
    }

    @FXML
    private void searchContactByKeyboard(){
        AtomicBoolean keysPressed = KeyboardHandler.handleEnterPressed(searchContactTextField);
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
        final String searchingStr = searchContactTextField.getText().trim();
        contactsListView.getItems().stream()
                .filter(item -> item.getCurrentContact().getFullName().toLowerCase().contains(searchingStr.toLowerCase()) )
                .findAny()
                .ifPresent(item -> {
                    contactsListView.getSelectionModel().select(item);
                    contactsListView.scrollTo(item);
                });
    }

    @FXML
    private void createCommunity(){
        if(!newCommunityNameTextField.getText().isEmpty()){
            JLocalCommunity localCommunity = new JLocalCommunity();
            contactsListView.getItems().filtered(contactCell -> contactCell.getCurrentContact().isChosen()).
                    forEach(contact -> localCommunity.addContactToCommunity(contact.getCurrentContact()));
            if(localCommunity.getCommunitySize().get() != 0){
                toCommunitiesWindow(localCommunity);
            }
        }
        else{
            newCommunityNameTextField.setFocusTraversable(true);
            communityNameLabel.setTextFill(Color.RED);
        }
    }

    private void toCommunitiesWindow(JLocalCommunity localCommunity) {
        localCommunity.setCommunityName(newCommunityNameTextField.getText());
        InfoContainer.addCommunity(localCommunity);
        List<String> members = localCommunity.getCommunityMembers()
                .stream()
                .map(elem -> elem.getPhoneNumber())
                .collect(Collectors.toList());
        BackMsg msg = new BackMsg.CreateCommunityJava(
                localCommunity.getCommunityName(),
                members
        );
        try {
            getUserStateRef().get().outQueue.put(msg);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToCommunitiesListWindowFXML);
    }

    @FXML
    private void toChatsWindow(){
        getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToChatsWindowFXML);
    }


    @FXML
    private void handleSearchContactKeyTyped(){
        searchContactTextField.addEventFilter(KeyEvent.KEY_TYPED, KeyboardHandler.maxLengthHandler(40));
    }

    @FXML
    private void handleСomNameKeyTyped(){
        newCommunityNameTextField.addEventFilter(KeyEvent.KEY_TYPED, KeyboardHandler.maxLengthHandler(40));
    }


}