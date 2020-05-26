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

import javax.swing.*;


public class MainWindowDataHandler extends DataHandler {

 /*   @FXML
    private TableView<TdApi.Chat> chatsList;*/

    @FXML
    private TableView<String> chatsList;

    @FXML
    private TableColumn<String, String> chatsColumn;

    @FXML
    private MenuButton profileSettings;

    @FXML
    private TextField searchMessageField;

    @FXML
    private JSplitPane splitPane;


    public MainWindowDataHandler() {
    }

    @Override
    public void setEncryWindow(EncryWindow encryWindow) {
        super.setEncryWindow(encryWindow);
        chatsList.setItems(encryWindow.getObservableChatListTest());
        initializeChats();
    }

    @FXML
    private void initializeChats(){
        //chatsColumn.setCellValueFactory(cellData -> cellData.getValue().g);
    }

    @FXML
    private ObservableList<TdApi.Chat> getObservableChatList(){
        ObservableList<TdApi.Chat> observableChatList = FXCollections.observableArrayList();
        observableChatList.addAll(getUserStateRef().get().getChatList());
        return observableChatList;
    }

}