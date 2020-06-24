package org.javaFX.controller.impl.handler;

import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import org.drinkless.tdlib.TdApi;
import org.encryfoundation.tg.javaIntegration.JavaInterMsg;
import org.javaFX.EncryWindow;
import org.javaFX.controller.MainWindowBasicHandler;
import org.javaFX.model.JLocalCommunityMember;
import org.javaFX.util.KeyboardHandler;

import java.util.concurrent.atomic.AtomicBoolean;

public class ContactsMainWindowHandler extends MainWindowBasicHandler {

    @FXML
    private TableView<JLocalCommunityMember> contactsTable;

    @FXML
    private TableColumn<JLocalCommunityMember, String> contactsColumn;

    @FXML
    private TextArea searchContactTextArea;

    @FXML
    private ObservableList<JLocalCommunityMember> getObservableUserList(){
        ObservableList<JLocalCommunityMember> observableContactList = FXCollections.observableArrayList();
        for(Integer jUserId: getUserStateRef().get().getUsers().keySet()){
            TdApi.User user = getUserStateRef().get().getUsers().get(jUserId);
            if(!user.lastName.isEmpty())
                observableContactList.add(new JLocalCommunityMember(user.firstName, user.lastName));
        }
        return observableContactList;
    }

    @Override
    public void updateEncryWindow(EncryWindow encryWindow) {
        super.setEncryWindow(encryWindow);
        contactsTable.setItems(getObservableUserList());
        initializeTable();
        enableMenuBar();
    }

    @Override
    protected void initializeTable() {
        contactsColumn.setCellValueFactory(cellData -> cellData.getValue().getFullName());
    }

    @Override
    protected void clickItem() {
        getUserStateRef().get().setActiveDialog(jDialog);
        getUserStateRef().get().setActiveDialogArea(dialogTextArea);
        //TODO: userID не совпадает с chatID! решить конфликт
        JavaInterMsg msg = new JavaInterMsg.SetActiveChat(
                contactsTable.getSelectionModel().getSelectedItem().userIdProperty().get()
        );
        try {
            getUserStateRef().get().msgsQueue.put(msg);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        callButton.setVisible(true);
        callButton.setDisable(false);
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
        contactsTable.getItems().stream()
                .filter(item -> item.getFullName().getValueSafe().toLowerCase().contains(searchingStr.toLowerCase()) )
                .findAny()
                .ifPresent(item -> {
                    contactsTable.getSelectionModel().select(item);
                    contactsTable.scrollTo(item);
                });
    }
}
