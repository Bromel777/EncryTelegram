package org.javaFX.controller.impl.handler;

import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
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

import java.util.Comparator;
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
    private Separator separator;

    @FXML
    private Label notFoundInfoLabel;

    @FXML
    private Label nobodyChosenErrorLabel;

    @FXML
    private Label chooseTitleLabel;

    @FXML
    private ImageView searchImg;

    public CreateNewLocalCommunityHandler() {
        super();
    }

    @Override
    public void updateEncryWindow(EncryWindow encryWindow) {
        for(VBoxContactCell cell : contactsListView.getItems()){
            cell.setSeparatorLineSize(separator.getWidth()- 40);
        }
        super.updateEncryWindow(encryWindow);
    }

    private ObservableList<VBoxContactCell> getObservableUserList(){
        final String searchingStr = searchContactTextField.getText().trim();
        ObservableList<VBoxContactCell> observableList = getFilteredList(initTableBySubstr(searchingStr), searchingStr);
        observableList.sort(new Comparator<VBoxContactCell>() {
            @Override
            public int compare(VBoxContactCell o1, VBoxContactCell o2) {
                return o1.getCurrentContact().getFullName()
                        .compareTo(o2.getCurrentContact().getFullName());
            }
        });
        return observableList;
    }

    @Override
    protected void initChatsTable(){
        ObservableList<VBoxContactCell> observableUserList = getObservableUserList();
        contactsListView.setItems(observableUserList);
        shutDownScheduledService();
    }

    @FXML
    private void changeCheckBoxValue() {
        VBoxContactCell clickedCell = contactsListView.getSelectionModel().getSelectedItem();
        JSingleContact communityMember = clickedCell.getCurrentContact();
        refreshColors(clickedCell);
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
        contactsListView.setItems(getFilteredList(initTableBySubstr(searchingStr), searchingStr));
    }

    private ObservableList<VBoxContactCell> getFilteredList(ObservableList<VBoxContactCell> rawList, final String searchString ){
        rawList.sort( Comparator.comparing(contactCell -> ((VBoxContactCell)contactCell)
                .getCurrentContact().getFullName().toLowerCase().indexOf(searchString.toLowerCase())));
        return rawList;
    }

    private void refreshColors(VBoxContactCell activeCell){
        for(VBoxContactCell cell: contactsListView.getItems()){
            cell.resetPaneColor();
        }
        activeCell.updatePaneColor();
    }

    private ObservableList<VBoxContactCell> initTableBySubstr(String searchingStr){
        ObservableList<VBoxContactCell> observableList = FXCollections.observableArrayList();
        for(Long jUserId: getUserStateRef().get().getUsersMap().keySet()){
            TdApi.User user = getUserStateRef().get().getUsersMap().get(jUserId);
            if(!user.phoneNumber.isEmpty() && isUserNameOrSurnameContainsStr(user, searchingStr))
                observableList.add(
                        new VBoxContactCell(
                                new JSingleContact(user.firstName, user.lastName, user.phoneNumber, (long)user.id)));
        }
        if(observableList.size() == 0 ){
            notFoundInfoLabel.setVisible(true);
        }
        else {
            notFoundInfoLabel.setVisible(false);
        }
        return observableList;
    }

    private boolean isUserNameOrSurnameContainsStr(TdApi.User user, String searchingStr){
        return user.lastName.toLowerCase().contains(searchingStr.toLowerCase()) ||
                user.firstName.toLowerCase().contains(searchingStr.toLowerCase()) ||
                user.phoneNumber.toLowerCase().contains(searchingStr.toLowerCase()) ;
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
            else {
                nobodyChosenErrorLabel.setVisible(true);
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

    @FXML
    private void handleSearchImg(){
        searchImg.setVisible(false);
        searchContactTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue) {
                if(searchContactTextField.getText().length() == 0){
                    searchImg.setVisible(true);
                }
            }
        });
    }

}