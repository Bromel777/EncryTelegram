package org.javaFX.controller.handlers;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Duration;
import org.javaFX.EncryWindow;
import org.javaFX.model.JChat;
import org.javaFX.util.observers.BasicObserver;
import org.javaFX.util.observers.JChatObserver;

import java.util.concurrent.atomic.AtomicInteger;

public class LocalCommunityHandler extends DataHandler{

    @FXML
    private TableView<JChat> chatsTable;

    @FXML
    private TableColumn<JChat, Long> rowNumber;

    @FXML
    private TableColumn<JChat, String> chatsNameColumn;


    @FXML
    private TableColumn<JChat, Long> chatsIDColumn;

    @FXML
    private TableColumn<JChat, CheckBox> checkBoxesColumn;

    @FXML
    private Button createButton;


    public LocalCommunityHandler() {
        chatListTableObserve(this);
    }

    private void chatListTableObserve(DataHandler controller){
        BasicObserver service = new JChatObserver(controller);
        service.setPeriod(Duration.seconds(1));
        service.start();
    }


    @Override
    public void updateEncryWindow(EncryWindow encryWindow) {
        super.setEncryWindow(encryWindow);
        chatsTable.setItems(getObservableChatList());
        initChatsTable();
    }

    private ObservableList<JChat> getObservableChatList(){
        ObservableList<JChat> observableChatList = FXCollections.observableArrayList();
        getUserStateRef().get().getChatList().forEach(
                chat -> observableChatList.add(new JChat(chat.title, "test", chat.id) )
        );
        return observableChatList;
    }

    private void initChatsTable(){
        chatsNameColumn.setCellValueFactory(cellData -> cellData.getValue().getTitle());
        chatsIDColumn.setCellValueFactory(cellData -> cellData.getValue().chatIdProperty().asObject());
        //checkBoxesColumn.setCellValueFactory(cellData -> new CheckBox().selectedProperty().asObject());
    }
}
