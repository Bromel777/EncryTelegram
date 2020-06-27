package org.javaFX.controller.impl.handler;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.encryfoundation.tg.javaIntegration.JavaInterMsg;
import org.javaFX.EncryWindow;
import org.javaFX.controller.MainWindowBasicHandler;
import org.javaFX.model.JChat;

public class MainWindowHandler extends MainWindowBasicHandler {

    @FXML
    private TableView<JChat> chatsTable;

    @FXML
    private TableColumn<JChat, String> chatsColumn;

    @FXML
    private TableColumn<JChat, String> lastMsgColumn;


    @Override
    public void updateEncryWindow(EncryWindow encryWindow) {
        super.setEncryWindow(encryWindow);
        chatsTable.setItems(getObservableChatList());
        initializeTable();
        enableMenuBar();
    }

    @FXML
    private ObservableList<JChat> getObservableChatList(){
        ObservableList<JChat> observableChatList = FXCollections.observableArrayList();
        getUserStateRef().get().getChatList().forEach(
                chat -> observableChatList.add(new JChat(chat.title, "stub", chat.id) )
        );
        return observableChatList;
    }

    @FXML
    protected void initializeTable() {
        chatsColumn.setCellValueFactory(cellData -> cellData.getValue().getTitle());
        lastMsgColumn.setCellValueFactory(cellData -> cellData.getValue().getLastMessage());
    }

    @FXML
    protected void clickItem() {
        getUserStateRef().get().setActiveDialog(jDialog);
        getUserStateRef().get().setActiveDialogArea(dialogTextArea);
        JavaInterMsg msg = new JavaInterMsg.SetActiveChat(
                chatsTable.getSelectionModel().getSelectedItem().chatIdProperty().get()
        );
        try {
            getUserStateRef().get().msgsQueue.put(msg);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        callButton.setVisible(true);
        callButton.setDisable(false);
    }

}