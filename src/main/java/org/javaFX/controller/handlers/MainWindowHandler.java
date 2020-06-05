package org.javaFX.controller.handlers;


import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;
import org.drinkless.tdlib.TdApi;
import org.javaFX.EncryWindow;
import org.javaFX.model.JChat;
import org.javaFX.model.JDialog;
import org.javaFX.util.observers.BasicObserver;
import org.javaFX.util.KeyboardHandler;
import org.javaFX.util.observers.JChatObserver;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainWindowHandler extends DataHandler {

    @FXML
    private TableView<JChat> chatsTable;

    @FXML
    private TableColumn<JChat, String> chatsColumn;

    @FXML
    private MenuButton profileSettings;

    @FXML
    private TextArea searchMessageArea;

    @FXML
    private TextArea sendMessageArea;

    @FXML
    private TextArea dialogArea;

    @FXML
    private Button sendMessageButton;

    private JDialog jDialog;

    public MainWindowHandler() {
        chatListObserve(this);
        createDialog();
    }

    private void chatListObserve(DataHandler controller){
        BasicObserver service = new JChatObserver(controller);
        service.setPeriod(Duration.seconds(3));
        service.start();
    }


    @Override
    public void updateEncryWindow(EncryWindow encryWindow) {
        super.setEncryWindow(encryWindow);
        chatsTable.setItems(getObservableChatList());
        initializeChats();
        //updateDialog();
    }

    @FXML
    private ObservableList<JChat> getObservableChatList(){
        ObservableList<JChat> observableChatList = FXCollections.observableArrayList();
        getUserStateRef().get().getChatList().forEach(
                chat -> observableChatList.add(new JChat(chat.title + "("+")") )
        );

        /*getUserStateRef().get().getChatList().forEach(
                chat -> observableChatList.add(new JChat(chat.title + "("+chat.unreadMentionCount +")") )
        );*/
        return observableChatList;
    }

    @FXML
    private void initializeChats() {
        chatsColumn.setCellValueFactory(cellData -> cellData.getValue().getTitle());
    }

    @FXML
    private void sendMessage(){
        String messageStr = sendMessageArea.getText().trim();
        if(!messageStr.isEmpty()) {
            String preparedStr = "You:\t" + messageStr;
            StringBuffer localDialogHistory = jDialog.getContent();
            localDialogHistory.append(preparedStr + "\n");
            dialogArea.setText(localDialogHistory.toString());
            sendMessageArea.setText("");
            jDialog.setContent(localDialogHistory);
        }
    }

    @FXML
    private void sendMessageByKeyboard(){
        AtomicBoolean[] keysPressed = KeyboardHandler.handleShiftEnterPressed(sendMessageArea);
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (keysPressed[0].get() && keysPressed[1].get()) {
                    sendMessage();
                }
            }
        }.start();
    }

    @FXML
    private void findMessageByKeyboard(){
        AtomicBoolean keysPressed = KeyboardHandler.handleEnterPressed(searchMessageArea);
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if ( keysPressed.get() ) {
                    findMessage();
                }
            }
        }.start();
    }

    private void findMessage(){
        final String searchingStr = searchMessageArea.getText().trim();
        searchMessageArea.setText(searchingStr);
        if(!searchingStr.isEmpty()) {
            StringBuffer localDialogHistory = jDialog.getContent();
            String [] messagesArray = localDialogHistory.toString().split("\n");
            StringBuffer results = new StringBuffer();
            Arrays.stream(messagesArray)
                    .filter(str -> str.toLowerCase().contains(searchingStr.toLowerCase()))
                    .forEach(str -> results.append(str+"\n"));
            dialogArea.setText(results.toString());
        }
    }


    private void createDialog(){
        jDialog = new JDialog("title stub");
    }

    private void updateDialog(int chatIndex){
        /*TdApi.Chat chatBean = getUserStateRef().get().getChatList().get(chatIndex);
        jDialog = new JDialog(chatBean.title);
        StringBuffer localDialogHistory = jDialog.getContent();
        dialogArea.setText(localDialogHistory.toString());
        jDialog.setContent(localDialogHistory);*/
    }

    @FXML
    private void showMenu(){
        System.out.println("show menu");
    }

    @FXML
    private void showChats(){
        System.out.println("show chats");
    }

    @FXML
    private void showPrivateChats(){
        System.out.println("show private chats");
    }

    @FXML
    private void showOptions(){
        System.out.println("show options");
    }
}