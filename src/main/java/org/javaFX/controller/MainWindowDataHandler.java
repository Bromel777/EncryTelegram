package org.javaFX.controller;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import org.drinkless.tdlib.TdApi;
import org.javaFX.EncryWindow;
import org.javaFX.model.JChat;
import org.javaFX.model.JUserState;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class MainWindowDataHandler extends DataHandler {

    @FXML
    private TableView<JChat> chatsTable;

    @FXML
    private TableColumn<JChat, String> chatsColumn;

    @FXML
    private MenuButton profileSettings;

    @FXML
    private TextField searchMessageField;


    public MainWindowDataHandler() {
    }

    @Override
    public void setEncryWindow(EncryWindow encryWindow) {
        super.setEncryWindow(encryWindow);
        chatsTable.setItems(getObservableChatList());
        initializeChats();

        /*chatsTable.setItems(getObservableChatListTest());
        initializeChatsTest();*/
    }

    @FXML
    private ObservableList<JChat> getObservableChatList(){
        ObservableList<JChat> observableChatList = FXCollections.observableArrayList();
        /*
       getUserStateRef().get().getChatList().forEach(
                chat -> observableChatList.add(new JChat(chat.title, chat.lastMessage.content.toString() ) )
        );
       */
        AtomicReference<JUserState> atomicReference = getUserStateRef();
        JUserState userState = atomicReference.get();
        List<TdApi.Chat> chatList = userState.getChatList();
        chatList.forEach(
                chat -> observableChatList.add(new JChat(chat.title, chat.lastMessage.content.toString() ) )
        );
        return observableChatList;
    }


    @FXML
    private void initializeChats() {
        chatsColumn.setCellValueFactory(cellData -> cellData.getValue().getTitle());
    }


  /*  @FXML
    private void initializeChatsTest() {
        chatsColumn.setCellValueFactory(cellData -> cellData.getValue().getTitle());
    }

    private ObservableList<JChat> getObservableChatListTest(){
        ObservableList<JChat> observableChatList = FXCollections.observableArrayList();
        JChat chat1 = new JChat(new SimpleStringProperty("chat1"), new SimpleStringProperty("last message1"));
        JChat chat2 = new JChat(new SimpleStringProperty("chat2"), new SimpleStringProperty("last message2"));
        JChat chat3 = new JChat(new SimpleStringProperty("chat3"), new SimpleStringProperty("last message3"));
        observableChatList.add(chat1);
        observableChatList.add(chat2);
        observableChatList.add(chat3);
        for(JChat s : observableChatList){
            System.out.println(s);
        }
        return observableChatList;
    }
*/
}