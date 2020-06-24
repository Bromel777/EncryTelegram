package org.javaFX.controller.impl.handler;

import javafx.animation.AnimationTimer;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.drinkless.tdlib.TdApi;
import org.javaFX.EncryWindow;
import org.javaFX.controller.DataHandler;
import org.javaFX.controller.impl.dialog.EnterCommunityNameDialogController;
import org.javaFX.model.JLocalCommunity;
import org.javaFX.model.JLocalCommunityMember;
import org.javaFX.util.KeyboardHandler;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


public class LocalCommunityHandler extends DataHandler {

    @FXML
    private TableView<JLocalCommunityMember> chatsTable;

    @FXML
    private TableColumn<JLocalCommunityMember, Integer> rowNumberColumn;

    @FXML
    private TableColumn<JLocalCommunityMember, String> chatsNameColumn;

    @FXML
    private TableColumn<JLocalCommunityMember, String> phoneNumberColumn;

    @FXML
    private TableColumn<JLocalCommunityMember, CheckBox> checkBoxesColumn;

    @FXML
    private TextArea searchContactTextArea;

    private ScheduledExecutorService service;

    private void runDelayedInitialization(){
        service = Executors.newSingleThreadScheduledExecutor();
        service.schedule(() -> updateEncryWindow(getEncryWindow()), 1, TimeUnit.SECONDS);
    }

    public LocalCommunityHandler() {
        runDelayedInitialization();
    }

    @Override
    public void updateEncryWindow(EncryWindow encryWindow) {
        super.setEncryWindow(encryWindow);
        chatsTable.setItems(getObservableUserList());
        initChatsTable();
    }


    private ObservableList<JLocalCommunityMember> getObservableUserList(){
        ObservableList<JLocalCommunityMember> observableChatList = FXCollections.observableArrayList();
        for(Integer jUserId: getUserStateRef().get().getUsers().keySet()){
            TdApi.User user = getUserStateRef().get().getUsers().get(jUserId);
            if(!user.lastName.isEmpty())
                observableChatList.add(new JLocalCommunityMember(user.firstName, user.lastName, user.phoneNumber, (long)user.id));
        }
        return observableChatList;
    }

    private void initChatsTable(){
        rowNumberColumn.setCellValueFactory(cellDate -> new SimpleIntegerProperty(cellDate.getValue().getRowNumber().get()).asObject() );
        chatsNameColumn.setCellValueFactory(cellData -> cellData.getValue().getFullName());
        phoneNumberColumn.setCellValueFactory(cellData -> cellData.getValue().getPhoneNumber());
        checkBoxesColumn.setCellValueFactory(cellData -> {
            JLocalCommunityMember jLocalCommunityMember = cellData.getValue();
            CheckBox checkBox = new CheckBox();
            checkBox.selectedProperty().setValue(jLocalCommunityMember.isChosenBoolean());
            checkBox.selectedProperty().addListener((ov, old_val, new_val) ->
                    jLocalCommunityMember.setBooleanChosen(new_val));
            return new SimpleObjectProperty<>(checkBox);
        });
        service.shutdown();
    }

    @FXML
    private void createButtonAction(){
        JLocalCommunity localCommunity = new JLocalCommunity();
        chatsTable.getItems().filtered(JLocalCommunityMember::isChosenBoolean).
               forEach(localCommunity::addContactToCommunity);
        JLocalCommunityMember.resetRowNumber();
        launchEnterNameDialog(localCommunity);
    }

    @FXML
    private void changeCheckBoxValue() {
        JLocalCommunityMember communityMember = chatsTable.getSelectionModel().getSelectedItem();
        if(communityMember.getUserId() > 0L ){
            boolean isChosen = communityMember.isChosenBoolean();
            communityMember.setBooleanChosen(!isChosen);
        }
    }

    private void launchEnterNameDialog(JLocalCommunity localCommunity){
        FXMLLoader loader = new FXMLLoader();
        Stage dialogStage = createDialogByPathToFXML(loader, EncryWindow.pathToLocalCommunityNameDialogFXML);
        EnterCommunityNameDialogController controller = loader.getController();
        controller.setEncryWindow(getEncryWindow());
        controller.setDialogStage(dialogStage);
        controller.setLocalCommunity(localCommunity);
        controller.setState(getUserStateRef());
        dialogStage.show();
    }

    @FXML
    private void searchContactByKeyboard(){
        AtomicBoolean keysPressed = KeyboardHandler.handleEnterPressed(searchContactTextArea);
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
        final String searchingStr = searchContactTextArea.getText().trim();
        chatsTable.getItems().stream()
                .filter(item -> item.getFullName().getValueSafe().toLowerCase().contains(searchingStr.toLowerCase()) )
                .findAny()
                .ifPresent(item -> {
                    chatsTable.getSelectionModel().select(item);
                    chatsTable.scrollTo(item);
                });
    }

}