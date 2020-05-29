package org.javaFX.controller.handlers;


import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;
import org.drinkless.tdlib.TdApi;
import org.javaFX.EncryWindow;
import org.javaFX.controller.DataHandler;
import org.javaFX.model.JChat;
import org.javaFX.util.Clock;
import org.javaFX.util.JChatTimerService;

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
        chatListObserve(this);
    }

    private void chatListObserve(DataHandler controller){
        ScheduledService<Object> service = new JChatTimerService(controller);
        service.setPeriod(Duration.seconds(3));
        service.start();
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

    @FXML
    private void initializeChats() {
        chatsColumn.setCellValueFactory(cellData -> cellData.getValue().getTitle());
    }

    @FXML private void sendMessage(){

    }
}