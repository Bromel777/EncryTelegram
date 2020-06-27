package org.javaFX.controller.impl.handler;

import javafx.animation.AnimationTimer;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import org.drinkless.tdlib.TdApi;
import org.encryfoundation.tg.javaIntegration.JavaInterMsg;
import org.javaFX.EncryWindow;
import org.javaFX.controller.DataHandler;
import org.javaFX.model.JLocalCommunity;
import org.javaFX.model.JSingleContact;
import org.javaFX.model.nodes.VBoxContactCell;
import org.javaFX.util.InfoContainer;
import org.javaFX.util.KeyboardHandler;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class _CreateNewLocalCommunityHandler extends DataHandler {

    @FXML
    private ListView<VBoxContactCell> contactsListView;

    @FXML
    private TextField newCommunityNameTextField;

    @FXML
    private TextField searchContactTextField;

    @FXML
    private Label communityNameLabel;

    private ScheduledExecutorService service;

    private void runDelayedInitialization(){
        service = Executors.newSingleThreadScheduledExecutor();
        service.schedule(() -> updateEncryWindow(getEncryWindow()), 1, TimeUnit.SECONDS);
    }

    public _CreateNewLocalCommunityHandler() {
        runDelayedInitialization();
    }

    @Override
    public void updateEncryWindow(EncryWindow encryWindow) {
        super.setEncryWindow(encryWindow);
        initChatsTable();
    }


    private ObservableList<VBoxContactCell> getObservableUserList(){
        ObservableList<VBoxContactCell> observableChatList = FXCollections.observableArrayList();
        for(Long jUserId: getUserStateRef().get().getUsersMap().keySet()){
            TdApi.User user = getUserStateRef().get().getUsersMap().get(jUserId);
            if(!user.lastName.isEmpty())
                observableChatList.add(
                        new VBoxContactCell(
                                new JSingleContact(user.firstName, user.lastName, user.phoneNumber, (long)user.id)));
        }
        return observableChatList;
    }

    private void initChatsTable(){
        contactsListView.setItems(getObservableUserList());
        service.shutdown();
    }

    @FXML
    private void changeCheckBoxValue() {
        JSingleContact communityMember = contactsListView.getSelectionModel().getSelectedItem().getCurrentContact();
        if(communityMember.getUserId() > 0L ){
            boolean isChosen = communityMember.isChosenBoolean();
            communityMember.setBooleanChosen(!isChosen);
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
                .filter(item -> item.getCurrentContact().getFullName().getValueSafe().toLowerCase().contains(searchingStr.toLowerCase()) )
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
            contactsListView.getItems().filtered(contactCell -> contactCell.getCurrentContact().isChosenBoolean()).
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
                .map(elem -> elem.getPhoneNumber().getValue())
                .collect(Collectors.toList());
        JavaInterMsg msg = new JavaInterMsg.CreateCommunityJava(
                localCommunity.getCommunityName(),
                members
        );
        try {
            getUserStateRef().get().msgsQueue.put(msg);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToCommunitiesListWindowFXML);
    }

    @FXML
    private void toChatsWindow(){
        getEncryWindow().launchWindowByPathToFXML(EncryWindow.pathToChatsWindowFXML);
    }

}