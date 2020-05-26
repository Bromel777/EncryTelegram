package org.javaFX.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.drinkless.tdlib.TdApi;
import org.javaFX.EncryWindow;

import javax.swing.table.TableColumn;

public class MainWindowDataHandler extends DataHandler {

    @FXML
    private TableView<TdApi.Chat> chatsList;

    @FXML
    private MenuButton profileSettings;

    @FXML
    private TextField searchMessageField;


    @Override
    public void setEncryWindow(EncryWindow encryWindow) {
        super.setEncryWindow(encryWindow);
        chatsList.setItems(getObservableChatList());
    }

    private ObservableList<TdApi.Chat> getObservableChatList(){
        ObservableList<TdApi.Chat> observableChatList = FXCollections.observableArrayList();
        observableChatList.addAll(getUserStateRef().get().getChatList());
        return observableChatList;
    }
}
