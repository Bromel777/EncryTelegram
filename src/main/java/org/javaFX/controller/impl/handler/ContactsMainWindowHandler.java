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
import org.javaFX.model.JSingleContact;
import org.javaFX.util.KeyboardHandler;

import java.util.concurrent.atomic.AtomicBoolean;

public class ContactsMainWindowHandler extends MainWindowBasicHandler {

    @FXML
    private TableView<JSingleContact> contactsTable;

    @FXML
    private TableColumn<JSingleContact, String> contactsColumn;

    @FXML
    private TextArea searchContactTextArea;

    @FXML
    private ObservableList<JSingleContact> getObservableUserList(){
        ObservableList<JSingleContact> observableContactList = FXCollections.observableArrayList();
        for(Long jUserId: getUserStateRef().get().getUsersMap().keySet()){
            TdApi.User user = getUserStateRef().get().getUsersMap().get(jUserId);
            if(!user.lastName.isEmpty()){
                observableContactList.add(new JSingleContact(user.firstName, user.lastName, user.id));
            }
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
                contactsTable.getSelectionModel().getSelectedItem().getUserId()
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
