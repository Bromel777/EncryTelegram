package org.javaFX.controller.handlers;


import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.javaFX.EncryWindow;
import org.javaFX.controller.DataHandler;
import org.javaFX.model.JChat;

public class MainWindowHandler extends DataHandler {

    @FXML
    private TableView<JChat> chatsTable;

    @FXML
    private TableColumn<JChat, String> chatsColumn;

    @FXML
    private MenuButton profileSettings;

    @FXML
    private TextField searchMessageField;

    @FXML
    private Button sendMessageButton;

    public MainWindowHandler() {
    }


    @Override
    public void updateEncryWindow(EncryWindow encryWindow) {
        super.setEncryWindow(encryWindow);
        chatsTable.setItems(getObservableChatList());
        initializeChats();
    }

    @FXML
    private ObservableList<JChat> getObservableChatList(){
        ObservableList<JChat> observableChatList = FXCollections.observableArrayList();
        getUserStateRef().get().getChatList().forEach(
                chat -> observableChatList.add(new JChat(chat.title, chat.lastMessage.content.toString() ) )
        );
        return observableChatList;
    }

    @FXML private void sendMessage(){

    }


    @FXML
    private void initializeChats() {
        chatsColumn.setCellValueFactory(cellData -> cellData.getValue().getTitle());
    }
}